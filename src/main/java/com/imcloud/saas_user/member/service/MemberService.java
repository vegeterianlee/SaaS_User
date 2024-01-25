package com.imcloud.saas_user.member.service;

import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.entity.Subscription;
import com.imcloud.saas_user.common.entity.UserSession;
import com.imcloud.saas_user.common.entity.enums.Product;
import com.imcloud.saas_user.common.entity.enums.UserRole;
import com.imcloud.saas_user.common.jwt.JwtUtil;
import com.imcloud.saas_user.common.repository.MemberRepository;
import com.imcloud.saas_user.common.repository.SubscriptionRepository;
import com.imcloud.saas_user.common.repository.UserSessionRepository;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.member.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
//import com.imcloud.saas_user.kafka.service.UserEventProducer;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;


@Service
@RequiredArgsConstructor
public class MemberService {

    @Value("${ADMIN_TOKEN}")
    private String adminTokenValue;

    @Value("${EMAIL_USERNAME}")
    private String myEmail;

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    //    private final UserEventProducer userEventProducer;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSessionRepository userSessionRepository;
    private final JavaMailSender mailSender;

   /* private final WebClient.Builder webClientBuilder;
    private  WebClient paymentClient;

    @PostConstruct
    public void init() {
        this.paymentClient = this.webClientBuilder.baseUrl("http://localhost:81/api/payments").build();
    }*/

    @Transactional
    public MemberResponseDto signup(SignupRequestDto signupRequestDto) {
        String userId = signupRequestDto.getUserId();
        String username = signupRequestDto.getUsername();
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        // userId 중복 확인
        Optional<Member> found = memberRepository.findByUserId(userId);
        if (found.isPresent()) {
            throw new IllegalArgumentException(ErrorMessage.USERID_DUPLICATION.getMessage());
        }

        Member newMember = Member.create(signupRequestDto, encodedPassword);
        memberRepository.save(newMember);
        return MemberResponseDto.of(newMember);
    }

    @Transactional
    public MemberResponseDto login(LoginRequestDto loginRequestDto, HttpServletRequest request, HttpServletResponse response) {

        // 사용자 확인
        Member member = memberRepository.findByUserId(loginRequestDto.getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new BadCredentialsException(ErrorMessage.WRONG_PASSWORD.getMessage());
        }

        // 현재 사용자의 활성 세션 수 확인
        int activeSessions = userSessionRepository.countActiveSessionsByMember(member.getUserId(),  LocalDateTime.now());
        Product product = member.getProduct();

        // 제한 확인
        if (activeSessions >= product.getNumOfAccounts()) {
            throw new IllegalArgumentException(ErrorMessage.TOO_MANY_SESSIONS.getMessage());
        }

        // 세션 정보 저장
        UserSession userSession = UserSession.builder()
                .userId(member.getUserId())
                .ipAddress(request.getRemoteAddr())
                .browserInfo(request.getHeader("User-Agent"))
                .loginTime(LocalDateTime.now())
                .userStatus(true)
                .expirationTime(LocalDateTime.now().plusHours(1)) // 1시간 후 만료
                .deletedFlag(false)
                .build();
        userSessionRepository.save(userSession);

        String jwtToken = jwtUtil.createToken(member.getUserId());
        return MemberResponseDto.of(member, jwtToken);
    }

    @Scheduled(fixedRate = 600000)  // 60,000 milliseconds = 1 minute
    public void invalidateExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<UserSession> expiredSessions = userSessionRepository.findByExpirationTimeBeforeAndUserStatus(now, true);
        for (UserSession session : expiredSessions) {
            session.setUserStatus(false);
        }
        userSessionRepository.saveAll(expiredSessions);
    }

    @Transactional
    public void logout(UserDetailsImpl userDetails, HttpServletRequest request) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.MEMBER_NOT_FOUND.getMessage())
        );


        // 현재 사용자의 세션 찾기
        List<UserSession> userSessions = userSessionRepository.findByUserIdAndIpAddressAndUserStatus(
                member.getUserId(), request.getRemoteAddr(), true);

        if (userSessions.isEmpty()) {
            throw new EntityNotFoundException(ErrorMessage.SESSION_NOT_FOUND.getMessage());
        }
        // 모든 매칭되는 세션 상태 업데이트
        for (UserSession userSession : userSessions) {
            userSession.setUserStatus(false);
        }
    }

    @Transactional(readOnly = true)
    public UserSession getLastSessionInfo(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage()));

        // 가장 최근의 세션을 가져옴
        Pageable topByOrderByLoginTimeDesc = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "loginTime"));
        List<UserSession> sessions = memberRepository.findLatestSessionsByUserId(member.getUserId(), topByOrderByLoginTimeDesc);

        if (sessions.isEmpty()) {
            throw new EntityNotFoundException("No session information found");
        }

        return sessions.get(0);
    }


    public MemberResponseDto checkUserInfo(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        return MemberResponseDto.of(member);
    }

    public Product checkProduct(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage()));

        return member.getProduct();
    }


    public ProfileResponseDto getUserByToken(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage()));

        return ProfileResponseDto.of(member);
    }

    @Transactional
    public void changePassword(UserDetailsImpl userDetails, String newPassword) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );
        // 새로운 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 비밀번호 변경
        member.setPassword(encodedPassword);

        // 변경된 멤버 저장
        memberRepository.save(member);
    }

    @Transactional
    public void resetPasswordAndSendEmail(String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.MEMBER_NOT_FOUND.getMessage()));

        String newPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(newPassword);
        member.setPassword(encodedPassword);
        memberRepository.save(member);

        sendPasswordResetEmail(member.getEmail(), newPassword);
    }

    public String generateRandomPassword() {
        // 숫자, 문자, 특수문자를 포함하는 문자열 정의
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = upperCaseLetters.toLowerCase();
        String numbers = "0123456789";
        String specialCharacters = "$@$!%*#?&";
        String combinedChars = upperCaseLetters + lowerCaseLetters + numbers + specialCharacters;

        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);

        // 각 카테고리에서 최소 하나의 문자를 무작위로 선택
        sb.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        sb.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        sb.append(numbers.charAt(random.nextInt(numbers.length())));
        sb.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));

        // 나머지 문자를 combinedChars에서 무작위로 선택
        for (int i = 4; i < 8; i++) {
            sb.append(combinedChars.charAt(random.nextInt(combinedChars.length())));
        }

        return sb.toString();
    }

    private void sendPasswordResetEmail(String email, String newPassword) {
        try {
            // JavaMailSender 객체로 이메일 전송
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(myEmail);
            helper.setTo(email);
            helper.setSubject("SDGUARD 서비스의 비밀번호가 초기화 되었습니다");

            // HTML 컨텐츠를 사용하여 이메일 본문 꾸미기
            String htmlContent = "<html>" +
                    "<body>" +
                    "<p>SDGUARD 서비스의 비밀번호가 성공적으로 초기화되었습니다.</p>" +
                    "<h4>임시 비밀번호:</h4>" +
                    "<p><span style='font-size:16px; color: #333;'>" + newPassword + "</span></p>" +
                    "<p>로그인 후 비밀번호를 즉시 변경해 주세요.</p>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true); // HTML을 사용하도록 true 설정
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송에 실패하였습니다.", e);
        }
    }

    @Transactional
    public boolean changeIsStorageEnabled(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        member.setRoleIsStorageEnabled();

        // 변경된 멤버 저장
        memberRepository.save(member);

        // 변경된 스토리지 사용 상태 반환
        return member.getIsStorageEnabled();
    }

    @Transactional
    public boolean changeIsKLTEnabled(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // 제품이 ENTERPRISE일 때만 적용
        if (Product.ENTERPRISE.equals(member.getProduct())) {
            member.setRoleisKLTEnabled();
            // 변경된 멤버 저장
            memberRepository.save(member);
        } else {
            // If the product is not ENTERPRISE, you can throw an exception or handle it as per your business logic.
            throw new EntityNotFoundException("KLT 모델 적용은 ENTERPRISE 제품을 이용하신 고객분들만 가능합니다");
        }

        // 변경된 스토리지 사용 상태 반환
        return member.getIsKltEnabled();
    }

    @Transactional
    public boolean checkObj(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // 변경된 스토리지 사용 상태 반환
        return member.getIsStorageEnabled();
    }

    @Transactional
    public boolean checkKLT(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // 변경된 스토리지 사용 상태 반환
        return member.getIsKltEnabled();
    }


    @Transactional
    public MemberResponseDto updateProfile(UserDetailsImpl userDetails, ProfileUpdateRequestDto requestDto) {
        // Check that the user exists
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // Update the member's profile information if the update fields are present
        if (requestDto.getUsername() != null) {
            member.setUsername(requestDto.getUsername());
        }
        if (requestDto.getPhone() != null) {
            member.setPhone(requestDto.getPhone());
        }
        if (requestDto.getEmail() != null) {
            member.setEmail(requestDto.getEmail());
        }
        if (requestDto.getInstitution() != null) {
            member.setInstitution(requestDto.getInstitution());
        }

        // Return a response
        return MemberResponseDto.of(member);
    }

    @Transactional
    public void deleteMember(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // 모든 관련 엔티티의 삭제 플래그 설정
//        markEntitiesAsDeleted(member.getUserId());

        // 구독 정보 조회
        Optional<Subscription> optionalSubscription = subscriptionRepository.findByUserIdAndIsActive(member.getUserId(), true);

        // 구독 정보가 존재하면 inactive로 바꾸기
        if (optionalSubscription.isPresent()) {
            Subscription subscription = optionalSubscription.get();
            subscription.setIsActive(false);
            subscription.setEndDateNow();
        }

        // 회원 삭제
        memberRepository.delete(member);
    }

//    private void markEntitiesAsDeleted(String userId) {
//        // 여기서는 FileActionHistory의 예시를 들고 있습니다. 필요한 다른 테이블도 같은 방식으로 처리할 수 있습니다.
//        List<FileActionHistory> fileActionHistories = fileActionHistoryRepository.findByUserId(userId);
//        for (FileActionHistory history : fileActionHistories) {
//            history.markAsDeleted();
//        }
//        // 다른 테이블들도 유사하게 처리
//
//        // 변경 사항 저장
//        fileActionHistoryRepository.saveAll(fileActionHistories);
//        // 다른 테이블들도 유사하게 저장
//    }

    @Transactional
    public void promoteToAdmin(UserDetailsImpl userDetails, String adminToken) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // check if admin token is valid
        if (!adminTokenValue.equals(adminToken)) {
            throw new BadCredentialsException(ErrorMessage.WRONG_ADMIN_TOKEN.getMessage());
        }

        // promote to admin
        member.setRole(UserRole.ADMIN);
    }

}
