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
    
    @Schema(type = "double", example = "200.0", description = "현재 월의 사용량이 저번 달 대비 몇 퍼센트인지 나타냅니다.")
    private Double usagePercentageIncrease;

    public static DataUsageResponseDto of(Long currentMonthUsage, Long lastMonthUsage, Double usagePercentageIncrease) {
        return DataUsageResponseDto.builder()
                .currentMonthUsage(currentMonthUsage)
                .lastMonthUsage(lastMonthUsage)
                .usagePercentageIncrease(usagePercentageIncrease)
                .build();
    }
}