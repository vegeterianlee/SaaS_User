package com.imcloud.saas_user.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequestDto {

    @Pattern(regexp = "^[a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ0-9]{2,8}$", message = "잘못된 양식의 닉네임을 입력하셨습니다.")
    @Schema(description = "/^[a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ0-9]{2,8}$/")
    private Optional<String> username;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d$@$!%*#?&]{8,}$", message = "잘못된 양식의 비밀번호를 입력하셨습니다.")
    @Schema(description = "/^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$/")
    private Optional<String> newPassword;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "잘못된 양식의 이메일을 입력하셨습니다.")
    @Schema(description = "/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$/")
    private Optional<String> email;

    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "잘못된 양식의 전화번호를 입력하셨습니다.")
    @Schema(description = "휴대폰 번호는 010-1234-5678 형식으로 작성")
    private Optional<String> phone;

    @Schema(description = "기관명")
    private Optional<String> institution;
}
