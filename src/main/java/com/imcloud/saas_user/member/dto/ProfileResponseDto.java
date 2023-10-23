package com.imcloud.saas_user.member.dto;

import com.imcloud.saas_user.common.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProfileResponseDto {
    @Schema(example = "apple123")
    private String username;

    @Schema(example = "user@gmail.com")
    private String email;

    @Schema(example = "010-1234-5678")
    private String phone;

    @Schema(example = "imcloud")
    private String institution;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ProfileResponseDto of(Member member){
        return ProfileResponseDto.builder()
                .username(member.getUsername())
                .email(member.getEmail())
                .phone(member.getPhone())
                .institution(member.getInstitution())
                .createdAt(member.getCreatedAt())
                .modifiedAt(member.getModifiedAt())
                .build();
    }
}
