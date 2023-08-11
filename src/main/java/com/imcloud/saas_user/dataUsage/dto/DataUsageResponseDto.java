package com.imcloud.saas_user.dataUsage.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class DataUsageResponseDto {

    @Schema(type = "integer", example = "1024", description = "현재 월의 데이터 사용량 (KB 단위)")
    private Long currentMonthUsage;

    @Schema(type = "integer", example = "512", description = "이전 월의 데이터 사용량 (KB 단위)")
    private Long lastMonthUsage;

    public static DataUsageResponseDto of(Long currentMonthUsage, Long lastMonthUsage) {
        return DataUsageResponseDto.builder()
                .currentMonthUsage(currentMonthUsage)
                .lastMonthUsage(lastMonthUsage)
                .build();
    }
}