package com.imcloud.saas_user.objStorage.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.*;
import com.imcloud.saas_user.common.entity.enums.FileActionType;
import com.imcloud.saas_user.common.entity.enums.PaymentStatus;
import com.imcloud.saas_user.common.entity.enums.Product;
import com.imcloud.saas_user.common.repository.*;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.fileAction.dto.FileActionDto;
import com.imcloud.saas_user.fileAction.dto.FileActionHistoryDto;
import com.imcloud.saas_user.objStorage.dto.StorageLogResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityNotFoundException;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private final FileActionHistoryRepository fileActionHistoryRepository;
    private final FileActionRepository fileActionRepository;

    // 암호화 저장
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    private static final String AES = "AES";
    private static final int AES_BLOCK_SIZE = 16;
    private static final Logger logger = LoggerFactory.getLogger(NaverObjectStorageService.class);


    @Value("${aes.encryption.key}")
    private String keyString;

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
    private SecretKey aesSecretKey;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        this.s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl, region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(keyString.getBytes(StandardCharsets.UTF_8));
        this.aesSecretKey = new SecretKeySpec(key, "AES");
    }

    private byte[] applyCustomPadding(byte[] dataBytes) {
        // AES 블록 크기에서 데이터 길이를 나눈 나머지를 뺍니다. 이는 필요한 패딩 길이를 계산합니다.
        int paddingLength = AES_BLOCK_SIZE - (dataBytes.length % AES_BLOCK_SIZE); // 예: 16 - (5 % 16) = 11

        // 새로운 바이트 배열을 만들어서, 원본 데이터와 패딩을 포함할 충분한 공간을 확보합니다.
        byte[] paddedData = new byte[dataBytes.length + paddingLength]; // 예: new byte[5 + 11] = new byte[16]

        // 원본 데이터를 새 배열의 시작 부분에 복사합니다.
        System.arraycopy(dataBytes, 0, paddedData, 0, dataBytes.length); // 예: 'Hello'를 paddedData[0]부터 paddedData[4]까지 복사

        // 패딩 바이트를 계산합니다. 패딩 길이를 바이트로 변환합니다.
        byte paddingByte = (byte) (paddingLength & 0xFF); // 예: (11 & 0xFF) = 11

        // 계산된 패딩 바이트로 나머지 배열을 채웁니다. 이는 원본 데이터 끝에서부터 배열 끝까지입니다.
        Arrays.fill(paddedData, dataBytes.length, paddedData.length, paddingByte);
        // 예: paddedData[5]부터 paddedData[15]까지 각각의 값을 11로 설정

        // 패딩이 적용된 배열을 반환합니다.
        return paddedData; // 예: ['H', 'e', 'l', 'l', 'o', 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11]
    }

    /*public String encodeData(byte[] dataBytes) throws GeneralSecurityException {
        byte[] paddedData = applyCustomPadding(dataBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesSecretKey, new IvParameterSpec(new byte[16]));
        byte[] encrypted = cipher.doFinal(paddedData);

        return Base64.getEncoder().encodeToString(encrypted);
    }*/

    public byte[] decodeData(byte[] encodedData) throws GeneralSecurityException {
        // Base64 디코딩
        byte[] decodedData = Base64.getDecoder().decode(encodedData);

        // AES/CBC/NoPadding 복호화
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesSecretKey, new IvParameterSpec(new byte[16]));
        byte[] decrypted = cipher.doFinal(decodedData);

        // Flask 스타일의 패딩 제거 로직
        int padValue = decrypted[decrypted.length - 1];
        if (padValue < 1 || padValue > 16) {
            throw new GeneralSecurityException("Invalid padding length");
        }

        // 패딩 제거
        int unpaddedLength = decrypted.length - padValue;
        byte[] unpaddedData = new byte[unpaddedLength];
        System.arraycopy(decrypted, 0, unpaddedData, 0, unpaddedLength);

        return unpaddedData;
    }

    private InputStream encryptStream(InputStream inputStream) throws GeneralSecurityException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        // 사용자 정의 패딩 적용
        byte[] data = byteArrayOutputStream.toByteArray();
        byte[] paddedData = applyCustomPadding(data);

        // AES 암호화
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesSecretKey, new IvParameterSpec(new byte[16])); // 동일한 IV 사용
        byte[] encryptedData;
        try {
            encryptedData = cipher.doFinal(paddedData); // 암호화
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            // 로그 기록
            logger.error("암호화 오류: " + e.getMessage(), e);
            throw e;
        }

        // Base64 인코딩
        return new ByteArrayInputStream(Base64.getEncoder().encode(encryptedData));
    }

    @Transactional
    public FileActionDto handleUpload(MultipartFile file, String fileName, UserDetailsImpl userDetails) throws IOException, GeneralSecurityException {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );
        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);
        String userId = member.getUserId();

        // 실제 업로드된 파일의 원본 파일 이름을 가져옵니다.
        String originalFileName = file.getOriginalFilename(); // 원본 파일 이름을 가져옵니다.
        String fileExtension = ""; // 파일 확장자를 저장할 변수 초기화

        // 원본 파일 이름에서 확장자를 추출합니다.
        if (originalFileName != null) {
            int extensionIndex = originalFileName.lastIndexOf(".");
            if (extensionIndex >= 0) {
                fileExtension = originalFileName.substring(extensionIndex); // 확장자 추출 (점 포함)
            }
        }

        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName + fileExtension; // 확장자를 포함하여 고유한 파일 이름 생성
        String objectKey = "de-identification/original/" + userId + "/" + uniqueFileName;

        try (InputStream fileInputStream = file.getInputStream()) {
            // 암호화된 스트림 생성
            InputStream encryptedStream = encryptStream(fileInputStream);

            // S3에 스트리밍 업로드
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(encryptedStream.available()); // 적절한 길이 설정 필요
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectKey, encryptedStream, metadata);
            s3.putObject(putRequest);

        } catch (GeneralSecurityException e) {
            throw new IOException("암호화 과정에서 오류가 발생했습니다.", e);
        } catch (AmazonServiceException e) {
            throw new IOException("S3 업로드 중 오류가 발생했습니다.", e);
        }

        // Create a StorageLog entry
        Long estimatedNetworkTraffic = (long) (file.getSize() * 1.10)/ 1024;
        StorageLog log = StorageLog.create(userId, fileName, estimatedNetworkTraffic, objectKey);
        FileAction fileAction = FileAction.create(fileName, objectKey, userId);
        storageLogRepository.save(log);
        fileActionRepository.save(fileAction);

        FileActionHistory fileActionHistory = FileActionHistory.create(fileAction, FileActionType.UPLOADED,member.getUserId());
        fileActionHistoryRepository.save(fileActionHistory);

        processAdditionalCharge(member, estimatedNetworkTraffic / 100);
        return FileActionDto.of(fileAction);
    }

    @Transactional(readOnly = true)
    public String getSignedUrl(String objectKey, UserDetailsImpl userDetails) throws IOException, GeneralSecurityException {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );
        checkIfStorageEnabled(member);

        // S3에서 암호화된 파일 다운로드
        S3Object s3Object = s3.getObject(bucketName, objectKey);
        InputStream encryptedDataStream = s3Object.getObjectContent();
        byte[] encryptedData = IOUtils.toByteArray(encryptedDataStream);

        // 복호화
        byte[] decryptedData = decodeData(encryptedData);

        // objectKey에서 uniqueFileName 추출
        String uniqueFileName = objectKey.substring(objectKey.lastIndexOf("/") + 1);

        // 복호화된 데이터를 S3에 임시 저장
        String tempObjectKey = "de-identification/temp/"+ member.getUserId() + "/"  + uniqueFileName;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(decryptedData.length);
        s3.putObject(new PutObjectRequest(bucketName, tempObjectKey, new ByteArrayInputStream(decryptedData), metadata));

        // 복호화된 파일에 대한 서명된 URL 생성
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, tempObjectKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(new Date(System.currentTimeMillis() + 3600 * 1000));
        URL signedUrl = s3.generatePresignedUrl(urlRequest);

        // 임시 파일 삭제 (옵션: 지연 삭제를 위해 별도의 삭제 로직 구현 필요)
//        s3.deleteObject(bucketName, tempObjectKey);

        return signedUrl.toString();
    }

    @Transactional(readOnly = true)
    public String getDownloadByString(String objectKey, UserDetailsImpl userDetails) throws IOException, GeneralSecurityException {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );
        checkIfStorageEnabled(member);

        // S3에서 암호화된 파일 다운로드 및 복호화
        S3Object s3Object = s3.getObject(bucketName, objectKey);
        InputStream encryptedDataStream = s3Object.getObjectContent();

        // 복호화된 스트림을 CSV 형식의 문자열로 변환
        String csvString = convertToCSV(encryptedDataStream); // 변경된 메서드 호출 방식
        return csvString;
    }

    private String convertToCSV(InputStream inputStream) throws IOException {
        Charset euckrCharset = Charset.forName("EUC-KR");
        Reader reader = new BufferedReader(new InputStreamReader(inputStream, euckrCharset));
        StringWriter writer = new StringWriter();

        try (CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            // 첫 번째 레코드(헤더)를 추출하고 작성
            String header = parser.getHeaderMap().keySet().stream()
                    .collect(Collectors.joining(","));
            writer.append(header).append("\n");

            // 각 레코드를 CSV 형식으로 변환하여 작성
            for (CSVRecord record : parser) {
                writer.append(String.join(",", record)).append("\n");
            }
        }
        return writer.toString();
    }


    @Transactional
    public boolean toggleIsDeidentifiedTarget(UserDetailsImpl userDetails, Long storageLogId) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);

        FileAction fileAction = fileActionRepository.findById(storageLogId)
                .orElseThrow(() -> new EntityNotFoundException("StorageLog not found with id: " + storageLogId));


        FileActionHistory fileActionHistory = FileActionHistory.create(fileAction, FileActionType.UPLOADED, member.getUserId());

        // 현재 IsDeidentifiedTarget 값 토글
        boolean newIsDeidentifiedTarget = !fileAction.getIsDeidentifiedTarget();
        fileAction.setIsDeidentifiedTarget(newIsDeidentifiedTarget);

        // FileActionHistory의 FileActionType을 설정합니다.
        if (newIsDeidentifiedTarget) {
            fileActionHistory.setActionType(FileActionType.TOBE_DEIDENTIFICATION);
            fileActionHistoryRepository.save(fileActionHistory);
        }

        fileActionRepository.save(fileAction);

        // 변경된 값을 반환
        return newIsDeidentifiedTarget;
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
    public Page<FileActionDto> findFileActionsWithDeidentifiedTarget(UserDetailsImpl userDetails, Integer page, Integer size) {
        String userId = userDetails.getUser().getUserId();

        // Ensure storage is enabled for the user
        checkIfStorageEnabled(memberRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found")));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        // find FileActions where isDeidentifiedTarget is true
        Page<FileAction> fileActionsPage = fileActionRepository
                .findByUserIdAndIsDeidentifiedTarget(userId, true, pageable);

        return fileActionsPage.map(FileActionDto::of);
    }

    @Transactional(readOnly = true)
    public Page<FileActionDto> findRecentFileActions(UserDetailsImpl userDetails, Integer page, Integer size) {
        String userId = userDetails.getUser().getUserId();

        // Ensure storage is enabled for the user
        checkIfStorageEnabled(memberRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found")));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        // Calculate the date 7 days ago
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // find FileActions for the userId stored within the last 7 days
        Page<FileAction> fileActionsPage = fileActionRepository
                .findByUserIdAndRecent(userId, sevenDaysAgo, pageable);

        return fileActionsPage.map(FileActionDto::of);
    }

    @Transactional(readOnly = true) // 트랜잭션을 읽기 전용으로 설정
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

        // 사용자 정보를 확인하고, 존재하지 않으면 예외를 발생시킵니다.
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage()));

        // 사용자가 스토리지를 사용할 수 있는지 확인합니다.
        checkIfStorageEnabled(member);

        // 페이지 요청 객체를 생성합니다. 페이지 번호는 0부터 시작하므로 1을 빼줍니다. 결과는 'id' 필드 기준으로 내림차순 정렬됩니다.
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        // 동적 쿼리 조건을 생성합니다. 이 조건은 검색 필터링에 사용됩니다.
        Specification<FileAction> spec = FileActionSpecifications.withDynamicQuery(
                toBeDeidentified, member.getUserId(), fileName, objectKey,
                storedAtStart, storedAtEnd, isDeidentifiedAtStart, isDeidentifiedAtEnd);

        // 조건에 맞는 FileAction을 조회합니다. 결과는 페이지 형태로 반환됩니다.
        Page<FileAction> fileActionsPage = fileActionRepository.findAll(spec, pageable);

        // 조회된 FileAction 엔티티를 FileActionDto로 변환하여 반환합니다.
        return fileActionsPage.map(FileActionDto::of);
    }

    @Transactional(readOnly = true)
    public Page<FileActionHistoryDto> searchFileActionHistories(
            UserDetailsImpl userDetails,
            String fileName,
            String objectKey,
            FileActionType actionType,
            LocalDateTime actionTimeStart,
            LocalDateTime actionTimeEnd,
            int page,
            int size) {

        // 사용자 정보를 확인하고, 존재하지 않으면 예외를 발생시킵니다.
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage()));

        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);

        // 페이지 요청 객체를 생성합니다. 페이지 번호는 0부터 시작하므로 1을 빼줍니다. 결과는 'id' 필드 기준으로 내림차순 정렬됩니다.
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        Specification<FileActionHistory> spec = FileActionHistorySpecifications.createSpecification(
                fileName,
                member.getUserId(),
                objectKey,
                actionType,
                actionTimeStart,
                actionTimeEnd);

        Page<FileActionHistory> histories = fileActionHistoryRepository.findAll(spec, pageable); // 동적 쿼리를 사용하여 데이터를 조회합니다.
        return histories.map(FileActionHistoryDto::of); // 조회된 데이터를 FileActionHistoryDto로 변환하여 반환합니다.
    }


    @Transactional
    public void deleteFile(String objectKey, UserDetailsImpl userDetails) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // Check if storage is enabled for the user
        checkIfStorageEnabled(member);

        // Delete the corresponding log from the database
        FileAction fileAction = fileActionRepository.findByObjectKey(objectKey).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.STORAGELOG_NOT_FOUND.getMessage())
        );

        FileActionHistory fileActionHistory = FileActionHistory.create(fileAction, FileActionType.DELETED, member.getUserId());
        fileActionHistoryRepository.save(fileActionHistory);

        // Delete the object from S3 bucket
        s3.deleteObject(bucketName, objectKey);
        fileActionRepository.delete(fileAction);
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

