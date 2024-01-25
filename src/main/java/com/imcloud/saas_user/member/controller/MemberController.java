package com.imcloud.saas_user.member.controller;

import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.entity.UserSession;
import com.imcloud.saas_user.common.entity.enums.Product;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.member.dto.*;
import com.imcloud.saas_user.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    @Operation(summary = "회원 가입 (create membership)",
            description = "userId must be 6-16 characters long and contain at least one number and one lowercase letter.\n" +
                    "Password must be 8 characters or longer, contain at least one number, and one uppercase or lowercase letter. It may also contain special characters like $@$!%*#?&.\n" +
                    "username must be 2-8 characters long and can include alphabets, numbers, and Korean characters.")
    public ApiResponse<MemberResponseDto> signup(@RequestBody @Valid SignupRequestDto signupRequestDto) {
        return ApiResponse.successOf(HttpStatus.CREATED, memberService.signup(signupRequestDto));
    }

    @GetMapping("/last-session")
    @Operation(summary = "사용자의 마지막 세션 정보 조회 (get user's last session info)")
    public ApiResponse<UserSession> getLastSessionInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.OK, memberService.getLastSessionInfo(userDetails));
    }

    @PostMapping("/login")
    @SecurityRequirements()
    @Operation(summary = "로그인 (login)")
    public ApiResponse<MemberResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto,
            @Parameter(hidden = true) HttpServletResponse response,
            @Parameter(hidden = true) HttpServletRequest request
    ) {
        return ApiResponse.successOf(HttpStatus.OK, memberService.login(loginRequestDto, request, response));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 (logout)" , description = "Required because we need to limit the number of concurrent users by expiring the session. This is just a session expiration")
    public ApiResponse<String> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(hidden = true) HttpServletRequest request
    ) {
        memberService.logout(userDetails, request);
        return ApiResponse.successOf(HttpStatus.OK, "로그아웃 성공");
    }

    @PostMapping("/updateProfile")
    @Operation(summary = "프로필 수정", description = "username, email, password, institution can modfied (Modifying is optional)")
    public ApiResponse<MemberResponseDto> update(
            @RequestBody @Valid ProfileUpdateRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.OK, memberService.updateProfile(userDetails, dto));
    }

    @GetMapping("/userInfo")
    @SecurityRequirements()
    @Operation(summary = "유저 정보확인", description = "로그인 후 확인되는 유저정보를 토큰 인증을 통해서만 확인하는 로직")
    public ApiResponse<MemberResponseDto> checkUserInfo(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        return ApiResponse.successOf(HttpStatus.OK, memberService.checkUserInfo(userDetails));
    }

    @GetMapping("/product")
    @Operation(summary = "사용자 제품 확인", description = "로그인한 사용자의 제품 정보를 반환합니다.")
    public ApiResponse<Product> checkUserProduct(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Product product = memberService.checkProduct(userDetails);
        return ApiResponse.successOf(HttpStatus.OK, product);
    }

    @GetMapping("/getProfile")
    @Operation(summary = "토큰으로 member 정보 조회 (Query member profile with token)", description = "Query member information with token")
    public ApiResponse<ProfileResponseDto> getUserByToken(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.OK, memberService.getUserByToken(userDetails));
    }

    @PostMapping("/resetPassword")
    @SecurityRequirements()
    @Operation(summary = "비밀번호 재설정", description = "사용자의 비밀번호를 재설정하고 새 비밀번호를 이메일로 전송합니다.")
    public ApiResponse<String> resetPassword(
            @RequestParam String userId) {
        memberService.resetPasswordAndSendEmail(userId);
        return ApiResponse.successOf(HttpStatus.OK, "비밀번호 리셋 완료");
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
    @Operation(summary = "Membership Withdrawal", description = "IsActive in subscription changed to 0x00")
    public ApiResponse<String> deleteMember(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails){
        memberService.deleteMember(userDetails);
        return ApiResponse.successOf(HttpStatus.OK, "회원 탈퇴 완료");
    }

    @PostMapping("/admin")
    @Operation(summary = "Role change Admin", description = "Change to Administrator Completed")
    public ApiResponse<String> changeRole(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String adminToken){
        memberService.promoteToAdmin(userDetails, adminToken);
        return ApiResponse.successOf(HttpStatus.OK, "관리자로 변경 완료");
    }

    @PostMapping("/changeStorageStatus")
    @Operation(summary = "Toggle Storage Usage Status", description = "Toggle the status of isStorageEnabled for the user")
    public ApiResponse<String> changeStorageStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean isStorageEnabled = memberService.changeIsStorageEnabled(userDetails);
        String message = isStorageEnabled ? "스토리지 사용 활성화 완료" : "스토리지 사용 비활성화 완료";
        return ApiResponse.successOf(HttpStatus.OK, message);
    }

    @PostMapping("/changeKLTStatus")
    @Operation(summary = "Toggle KLT Usage Status", description = "Toggle the status of isKLTEnabled for the user")
    public ApiResponse<String> changeKLTStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean isKLTEnabled = memberService.changeIsKLTEnabled(userDetails);
        String message = isKLTEnabled ? "KLT 사용 활성화 완료" : "KLT 사용 비활성화 완료";
        return ApiResponse.successOf(HttpStatus.OK, message);
    }

    @GetMapping("/checkStorage")
    @Operation(summary = "Check Storage Usage Status", description = "Check the status of isStorageEnabled for the user")
    public ApiResponse<Boolean> checkStorageStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean isStorageEnabled = memberService.checkObj(userDetails);
        return ApiResponse.successOf(HttpStatus.OK, isStorageEnabled);
    }

    @GetMapping("/checkKLT")
    @Operation(summary = "Check KLT Usage Status", description = "Check the status of isKLTEnabled for the user")
    public ApiResponse<Boolean> checkKLTStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean isKLTEnabled = memberService.checkKLT(userDetails);
        return ApiResponse.successOf(HttpStatus.OK, isKLTEnabled);
    }
}
