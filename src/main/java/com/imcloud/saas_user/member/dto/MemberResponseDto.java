package com.imcloud.saas_user.member.dto;

import com.imcloud.saas_user.common.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MemberResponseDto {
    @Schema(type = "integer", example = "2")
    private Long id;

    @Schema(example = "userId")
    private String userId;

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

    public static MemberResponseDto of(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .userId(member.getUserId())
                .email(member.getEmail())
                .phone(member.getPhone())
                .institution(member.getInstitution())
                .createdAt(member.getCreated_at())
                .modifiedAt(member.getModified_at())
                .build();
    }

    /*public static MemberResponseDto ofHasBlog(Member member, Long blogId) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .userId(member.getUserId())
                .email(member.getEmail())
                .createdAt(member.getCreatedAt())
                .modifiedAt(member.getModifiedAt())
                .build();
    }*/
}