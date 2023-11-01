package com.imcloud.saas_user.kltLog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class KLTLogResponseDto {

    @Schema(type = "integer", example = "10", description = "현재 월의 API 호출 횟수")
    private Long currentMonthApiCalls;

    @Schema(type = "integer", example = "5", description = "이전 월의 API 호출 횟수")
    private Long lastMonthApiCalls;

    @Schema(type = "double", example = "100.0", description = "현재 월의 API 호출 횟수가 저번 달 대비 몇 퍼센트 증가했는지 나타냅니다.")
    private Double apiCallsPercentageIncrease;

    @Schema(type = "integer", example = "1024", description = "현재 월의 네트워크 트래픽 (KB 단위)")
    private Long currentMonthTraffic;

    @Schema(type = "integer", example = "512", description = "이전 월의 네트워크 트래픽 (KB 단위)")
    private Long lastMonthTraffic;

    @Schema(type = "double", example = "100.0", description = "현재 월의 네트워크 트래픽이 저번 달 대비 몇 퍼센트 증가했는지 나타냅니다.")
    private Double trafficPercentageIncrease;

    public static KLTLogResponseDto of(Long currentMonthApiCalls, Long lastMonthApiCalls, Double apiCallsPercentageIncrease,
                                       Long currentMonthTraffic, Long lastMonthTraffic, Double trafficPercentageIncrease) {
        return KLTLogResponseDto.builder()
                .currentMonthApiCalls(currentMonthApiCalls)
                .lastMonthApiCalls(lastMonthApiCalls)
                .apiCallsPercentageIncrease(apiCallsPercentageIncrease)
                .currentMonthTraffic(currentMonthTraffic)
                .lastMonthTraffic(lastMonthTraffic)
                .trafficPercentageIncrease(trafficPercentageIncrease)
                .build();
    }
}
