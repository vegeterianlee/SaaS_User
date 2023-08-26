package com.imcloud.saas_user.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import javax.validation.constraints.Email;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
public class SignupRequestDto {
    @Pattern(regexp = "^(?=.*?[0-9])(?=.*?[a-z]).{6,16}$", message = "잘못된 양식의 아이디를 입력했거나 중복 허용되지 않습니다. .")
    @Schema(example = "userId1", description = "/^(?=.*?[0-9])(?=.*?[a-z]).{6,16}$/")
    private String userId;

    @Pattern(regexp = "^[a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ0-9]{2,8}$", message = "잘못된 양식의 닉네임을 입력하셨거나 중복 허용되지 않습니다.")
    @Schema(example = "nickname", description = "/^[a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ0-9]{2,8}$/")
    private String username;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d$@$!%*#?&]{8,}$", message = "잘못된 양식의 비밀번호를 입력하셨습니다.")
    @Schema(example = "Password1", description = "/^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$/")
    private String password;

    //@Email
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "잘못된 양식의 이메일을 입력하셨습니다.")
    @Schema(example = "user2323@gmail.com", description = "/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$/")
    private String email;

    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "잘못된 양식의 전화번호를 입력하셨습니다.")
    @Schema(example = "010-1234-5678", description = "휴대폰 번호는 010-1234-5678 형식으로 작성")
    private String phone;

    @Schema(example = "imcloud", description = "기관명")
    private String institution;



   /* //@DateValid(message = "8자리의 yyyy-MM-dd 형식이어야 합니다.", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyyMMdd")
    @PastOrPresent
    @Schema(example = "2023-03-13", description = "/^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$/")
    private LocalDate birthday;*/


}
