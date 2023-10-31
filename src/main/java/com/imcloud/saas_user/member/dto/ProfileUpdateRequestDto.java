package com.imcloud.saas_user.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequestDto {

    @Nullable
    @Pattern(regexp = "^[a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ0-9]{2,8}$", message = "잘못된 양식의 닉네임을 입력하셨습니다.")
    @Schema(example = "nickname", description = "/^[a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ0-9]{2,8}$/")
    private String username;

    @Nullable
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "잘못된 양식의 이메일을 입력하셨습니다.")
    @Schema(example = "user2323@gmail.com", description = "/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$/")
    private String email;

    @Nullable
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "잘못된 양식의 전화번호를 입력하셨습니다.")
    @Schema(example = "010-1234-5678", description = "휴대폰 번호는 010-1234-5678 형식으로 작성")
    private String phone;

    @Nullable
    @Schema(example = "you're cloud", description = "기관명")
    private String institution;
}
