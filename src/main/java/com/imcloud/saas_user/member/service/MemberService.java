package com.imcloud.saas_user.member.service;

import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.entity.enums.UserRole;
import com.imcloud.saas_user.common.jwt.JwtUtil;
import com.imcloud.saas_user.common.repository.MemberRepository;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.member.dto.LoginRequestDto;
import com.imcloud.saas_user.member.dto.MemberResponseDto;
import com.imcloud.saas_user.member.dto.SignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    @Value("${ADMIN_TOKEN}")
    private String adminTokenValue;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponseDto signup(SignupRequestDto signupRequestDto) {
        String userId = signupRequestDto.getUserId();
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

    @Transactional(readOnly = true)
    public MemberResponseDto login(LoginRequestDto loginRequestDto, HttpServletResponse response) {

        // 사용자 확인
        Member member = memberRepository.findByUserId(loginRequestDto.getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new BadCredentialsException(ErrorMessage.WRONG_PASSWORD.getMessage());
        }

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(member.getUserId()));
        return MemberResponseDto.of(member);
    }


    public Boolean checkUserId(String userId) {
        return memberRepository.findByUserId(userId).isPresent();
    }


    public MemberResponseDto getUserByToken(UserDetailsImpl userDetails) {
        Member member = userDetails.getUser();
        return MemberResponseDto.of(member);
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
    public void deleteMember(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );
        // 회원 정보 삭제
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
        member.setRole(UserRole.Admin);
    }
}
