package com.imcloud.saas_user.objStorage.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.*;
import com.imcloud.saas_user.common.entity.enums.FileActionType;
import com.imcloud.saas_user.common.entity.enums.PaymentStatus;
import com.imcloud.saas_user.common.entity.enums.Product;
import com.imcloud.saas_user.common.repository.*;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.fileAction.dto.FileActionDto;
import com.imcloud.saas_user.objStorage.dto.StorageLogResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NaverObjectStorageService {

    private final MemberRepository memberRepository;
    private final StorageLogRepository storageLogRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FileActionRepository fileActionRepository;


    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpointUrl;
    private final String bucketName = "saas-di-bucket";

    @Value("${cloud.aws.region.static}")
    private String region;
    private AmazonS3 s3;

    @PostConstruct
    public void init() {
        this.s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl, region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }
    @Transactional
    public FileActionDto handleUpload(MultipartFile file, String fileName, UserDetailsImpl userDetails) throws IOException {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );
        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);

        String userId = member.getUserId();
        String uniqueUserId = UUID.randomUUID().toString() + "_" + userId;
        String objectKey = "de-identification/original/" + uniqueUserId + "/" + fileName;

        // If the member's product is ENTERPRISE, encode the data
        if(member.getProduct() == Product.ENTERPRISE) {
            byte[] encodedBytes = Base64.getEncoder().encode(file.getBytes());
            InputStream encodedInputStream = new ByteArrayInputStream(encodedBytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(encodedBytes.length);
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectKey, encodedInputStream, metadata);
            s3.putObject(putRequest);

        } else {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectKey, file.getInputStream(), metadata);
            s3.putObject(putRequest);
        }

        // Create a StorageLog entry
        Long estimatedNetworkTraffic = (long) (file.getSize() * 1.10)/ 1024;
        StorageLog log = StorageLog.create(userId, fileName, estimatedNetworkTraffic, objectKey);
        FileAction fileAction = FileAction.create(fileName, objectKey, userId);

        fileActionRepository.save(fileAction);
        storageLogRepository.save(log);

        processAdditionalCharge(member, estimatedNetworkTraffic / 100);
        return FileActionDto.of(fileAction);
    }

    @Transactional
    public boolean toggleToBeDeidentified(UserDetailsImpl userDetails, Long storageLogId) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        FileAction fileAction = fileActionRepository.findById(storageLogId)
                .orElseThrow(() -> new EntityNotFoundException("StorageLog not found with id: " + storageLogId));

        // 현재 값 토글
        fileAction.setToBeDeidentified(!fileAction.getToBeDeidentified());
        fileActionRepository.save(fileAction);

        // 변경된 값을 반환
        return fileAction.getToBeDeidentified();
    }

    public boolean checkToBeDeidentified(UserDetailsImpl userDetails, Long storageLogId) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        FileAction fileAction = fileActionRepository.findById(storageLogId)
                .orElseThrow(() -> new EntityNotFoundException("StorageLog not found with id: " + storageLogId));

        // 현재 상태 반환
        return fileAction.getToBeDeidentified();
    }

    @Transactional(readOnly = true)
    public List<String> listFiles(UserDetailsImpl userDetails, String folder) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);

        String prefix = "de-identification/" +  folder + "/" +member.getUserId() + "/" ;
        return s3.listObjectsV2(bucketName, prefix).getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FileActionDto> searchFiles(UserDetailsImpl userDetails,
                                           String fileName,
                                           String objectKey,
                                           Boolean toBeDeidentified,
                                           LocalDateTime storedAtStart,
                                           LocalDateTime storedAtEnd,
                                           LocalDateTime isDeidentifiedAtStart,
                                           LocalDateTime isDeidentifiedAtEnd,
                                           Integer page,
                                           Integer size) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage()));

        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);

        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<FileAction> spec = FileActionSpecifications.withDynamicQuery(
                toBeDeidentified, fileName, objectKey, storedAtStart, storedAtEnd, isDeidentifiedAtStart, isDeidentifiedAtEnd);

        Page<FileAction> fileActionsPage = fileActionRepository.findAll(spec, pageable);
        return fileActionsPage.map(FileActionDto::of);
    }


    @Transactional
    public void deleteFile(String objectKey, UserDetailsImpl userDetails) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);

        // Delete the object from S3 bucket
        s3.deleteObject(bucketName, objectKey);

        // Delete the corresponding log from the database
        FileAction fileAction = fileActionRepository.findByObjectKey(objectKey).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.STORAGELOG_NOT_FOUND.getMessage())
        );

        fileActionRepository.delete(fileAction);
    }

    @Transactional(readOnly = true)
    public String getSignedUrl(String objectKey, UserDetailsImpl userDetails) throws IOException {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);

        // If the member's product is ENTERPRISE, decode the data
        /*if (member.getProduct() == Product.ENTERPRISE) {
            S3Object s3Object = s3.getObject(bucketName, objectKey);
            InputStream objectData = s3Object.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(objectData);
            byte[] decodedBytes = Base64.getDecoder().decode(bytes);
            InputStream decodedInputStream = new ByteArrayInputStream(decodedBytes);

            String decodedObjectKey = "decoded/" + objectKey;
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(decodedBytes.length);
            s3.putObject(bucketName, decodedObjectKey, decodedInputStream, metadata);

            // Generate signed URL for decoded object
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, decodedObjectKey)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            URL signedUrl = s3.generatePresignedUrl(generatePresignedUrlRequest);
            return signedUrl.toString();
        } else */

        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        URL signedUrl = s3.generatePresignedUrl(generatePresignedUrlRequest);
        return signedUrl.toString();

    }

    @Transactional(readOnly = true)
    public Map<String, String> calculateFileStorageDuration(UserDetailsImpl userDetails) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);

        List<StorageLog> logs = storageLogRepository.findAllByUserId(member.getUserId());

        if (logs.isEmpty()) {
            throw new IllegalArgumentException("No logs found for the provided user ID");
        }

        return logs.stream()
                .collect(Collectors.toMap(
                        StorageLog::getObjectKey,
                        log -> Duration.between(log.getStoredAt(), LocalDateTime.now()).toHours() + " hours",
                        (existing, replacement) -> replacement // 이 부분이 중복 키 처리를 위한 로직입니다.
                ));

    }


    @Transactional(readOnly = true)
    public Map<String, Long> getNetworkTrafficForEachLog(UserDetailsImpl userDetails) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);
        List<StorageLog> logs = storageLogRepository.findAllByUserId(member.getUserId());

        return logs.stream()
                .sorted(Comparator.comparing(StorageLog::getStoredAt).reversed())  // 로그를 최근 순으로 정렬
                .collect(Collectors.toMap(
                        StorageLog::getObjectKey,
                        StorageLog::getNetworkTraffic,
                        (existing, replacement) -> replacement  // 중복될 경우 나중의 로그 값을 사용
                ));
    }


    private void checkIfStorageEnabled(Member member) {
        if (!member.getIsStorageEnabled()) {
            throw new EntityNotFoundException(ErrorMessage.STORAGE_NOT_ENABLED.getMessage());
        }
    }

    @Transactional
    public void processAdditionalCharge(Member member, Long estimatedNetworkTraffic) {
        int additionalChargeAmount = estimatedNetworkTraffic.intValue();

        // 먼저 해당 사용자의 PaymentStatus.UNPAID 상태인 Payment 객체를 조회합니다.
        Payment unpaidPayment = paymentRepository.findByUserIdAndPaymentStatusAndProductName(
                member.getUserId(), PaymentStatus.UNPAID, "OBJECTSTORAGE");
        if (unpaidPayment == null) {
            // 해당 객체가 없다면 새로운 Payment 객체를 생성합니다.
            Payment payment = Payment.builder()
                    .productName("OBJECTSTORAGE")
                    .userId(member.getUserId())
                    .totalPrice(additionalChargeAmount)
                    .paymentStatus(PaymentStatus.UNPAID)
//                    .subscription(subscription)
                    .build();
            paymentRepository.save(payment);

        } else {
            // 이미 PaymentStatus.UNPAID 상태인 객체가 있다면 추가 요금을 기존 금액에 더합니다.
            unpaidPayment.setTotalPrice(unpaidPayment.getTotalPrice() + additionalChargeAmount);
            paymentRepository.save(unpaidPayment);
        }
    }
}

