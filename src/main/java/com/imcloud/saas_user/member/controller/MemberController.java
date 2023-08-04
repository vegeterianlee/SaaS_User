package com.imcloud.saas_user.member.controller;

import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.member.dto.LoginRequestDto;
import com.imcloud.saas_user.member.dto.MemberResponseDto;
import com.imcloud.saas_user.member.dto.ProfileUpdateRequestDto;
import com.imcloud.saas_user.member.dto.SignupRequestDto;
import com.imcloud.saas_user.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Tag(name = "member")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/signup")
    @SecurityRequirements()
    @Operation(summary = "회원 가입", description = "userId은 영문숫자 조합 4자 이상, 10자 이하\n password은 영문숫자 조합 8자 이상, 15자 이하\n, username은 아무 문자 2자 이상 8자 이하")
    public ApiResponse<MemberResponseDto> signup(@RequestBody @Valid SignupRequestDto signupRequestDto) {
        return ApiResponse.successOf(HttpStatus.CREATED, memberService.signup(signupRequestDto));
    }

    @PostMapping("/login")
    @SecurityRequirements()
    @Operation(summary = "로그인")
    public ApiResponse<MemberResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        return ApiResponse.successOf(HttpStatus.OK, memberService.login(loginRequestDto, response));
    }

    @PostMapping("/updateProfile")
    @Operation(summary = "프로필 수정", description = "username, email, password, institution 수정 가능 (수정하지 않아도 됨)")
    public ApiResponse<MemberResponseDto> update(
            @RequestBody @Valid ProfileUpdateRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.OK, memberService.updateProfile(userDetails, dto));
    }

    @GetMapping("/userId/duplicate")
    @SecurityRequirements()
    @Operation(summary = "아이디 중복체크", description = "아이디 중복이면 true, 중복이 아니면 false")
    public ApiResponse<Boolean> checkEmail(@RequestParam String userId){
        return ApiResponse.successOf(HttpStatus.OK, memberService.checkUserId(userId));
    }

    @GetMapping
    @Operation(summary = "토큰으로 member 정보 조회", description = "토큰으로 member 정보 조회")
    public ApiResponse<MemberResponseDto> getUserByToken(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.OK, memberService.getUserByToken(userDetails));
    }

    @GetMapping("/changePw")
    @Operation(summary = "change Password", description = "if can change return String (비밀번호 변경 완료)")
    public ApiResponse<String> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String newPassword){
        memberService.changePassword(userDetails, newPassword);
        return ApiResponse.successOf(HttpStatus.OK, "비밀번호 변경 완료");
    }

    @GetMapping("/withdrawal")
    @Operation(summary = "Membership Withdrawal", description = "return String(회원 탈퇴 완료)")
    public ApiResponse<String> deleteMember(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails){
        memberService.deleteMember(userDetails);
        return ApiResponse.successOf(HttpStatus.OK, "회원 탈퇴 완료");
    }

    @GetMapping("/admin")
    @Operation(summary = "Role change Admin", description = "return String(관리자로 변경 완료)")
    public ApiResponse<String> changeRole(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String adminToken){
        memberService.promoteToAdmin(userDetails, adminToken);
        return ApiResponse.successOf(HttpStatus.OK, "관리자로 변경 완료");
    }
}
