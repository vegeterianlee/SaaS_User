package com.imcloud.saas_user.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.entity.enums.Product;
import com.imcloud.saas_user.common.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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

    @Schema(example = "USER")
    private UserRole role;

    @Schema(example = "STANDARD", description = "구독 상품은 STANDARD, PREMIUM, ENTERPRISE 중 하나입니다.")
    private Product product;

    @Schema(type = "integer", example = "854MB", description = "사용된 데이터 용량")
    private Long dataUsage;

    @Schema(type = "integer", example = "854MB", description = "충전된 토큰수(KB)")
    private Long userToken;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @Schema(example = "sample.jwt.token.here")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String jwttoken;


    public static MemberResponseDto of(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .userId(member.getUserId())
                .email(member.getEmail())
                .phone(member.getPhone())
                .institution(member.getInstitution())
                .role(member.getRole())
                .dataUsage(member.getDataUsage())
                .userToken(member.getUserToken())
                .product(member.getProduct())
                .createdAt(member.getCreatedAt())
                .modifiedAt(member.getModifiedAt())
                .build();
    }

    public static MemberResponseDto of(Member member, String token) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .userId(member.getUserId())
                .email(member.getEmail())
                .phone(member.getPhone())
                .institution(member.getInstitution())
                .role(member.getRole())
                .dataUsage(member.getDataUsage())
                .userToken(member.getUserToken())
                .product(member.getProduct())
                .createdAt(member.getCreatedAt())
                .modifiedAt(member.getModifiedAt())
                .jwttoken(token) // 토큰 설정
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