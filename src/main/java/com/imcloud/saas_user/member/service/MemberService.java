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


@Service
@RequiredArgsConstructor
public class MemberService {

    @Value("${ADMIN_TOKEN}")
    private String adminTokenValue;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
//    private final UserEventProducer userEventProducer;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSessionRepository userSessionRepository;

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


    public Boolean checkUserId(String userId) {
        return memberRepository.findByUserId(userId).isPresent();
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

        member.setRoleisKLTEnabled();

        // 변경된 멤버 저장
        memberRepository.save(member);

        // 변경된 스토리지 사용 상태 반환
        return member.getIsKLTEnabled();
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

    @Transactional(readOnly = true)
    public void deleteMember(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // 구독하고 있던 제품은 inactive로 바꾸기
        Subscription subscription = subscriptionRepository.findByUserIdAndIsActive(member.getUserId(), true).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.SUBSCRIPTION_NOT_FOUND.getMessage())
        );
        subscription.setIsActive(false);
        subscription.setEndDateNow();
        memberRepository.delete(member);
    }

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
