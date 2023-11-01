package com.imcloud.saas_user.kltLog.controller;

import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.kltLog.dto.KLTLogResponseDto;
import com.imcloud.saas_user.kltLog.service.KLTLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "KLTLog")
@RestController
@RequestMapping("/api/kltLog")
@RequiredArgsConstructor
public class KLTLogController {
    private final KLTLogService kltLogService;

    @GetMapping("/apiCallsBreakdown")
    @Operation(
            summary = "최근 3개월 간의 API 호출 분석 (Retrieve API calls breakdown for the last three months)",
            description = "Returns the API calls breakdown for each of the last three months for the user."
    )
    public ApiResponse<Map<String, Long>> getApiCallsForLastThreeMonths(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, Long> apiCallsBreakdown = kltLogService.getApiCallsForLastThreeMonths(userDetails.getUser().getUserId());
        return ApiResponse.successOf(HttpStatus.OK, apiCallsBreakdown);
    }

    @GetMapping("/totalNetworkTrafficForMonth")
    @Operation(summary = "이번 달의 네트워크 트래픽 사용량 조회 (Retrieve total network traffic for the month)",
            description = "Returns the total network traffic for the user for the current month.")
    public ApiResponse<Long> getTotalNetworkTrafficForMonth(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long networkTraffic = kltLogService.getTotalNetworkTrafficForMonth(userDetails.getUser().getUserId());
        return ApiResponse.successOf(HttpStatus.OK, networkTraffic);
    }

    @GetMapping("/totalApiCallsForMonth")
    @Operation(summary = "이번달의 API 호출 횟수 조회 (Retrieve total API calls for the month)",
            description = "Returns the total API calls for the user for the current month.")
    public ApiResponse<Long> getTotalApiCallsForMonth(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long apiCalls = kltLogService.getTotalApiCallsForMonth(userDetails.getUser().getUserId());
        return ApiResponse.successOf(HttpStatus.OK, apiCalls);
    }

    @GetMapping("/monthlyComparison")
    @Operation(summary = "저번달과 이번달의 API 호출 횟수 및 네트워크 트래픽 비교 (Compare API calls and network traffic between last month and this month)",
            description = "Returns the API calls and network traffic comparison between the current month and the previous month for the user.")
    public ApiResponse<KLTLogResponseDto> getMonthlyApiAndTrafficComparison(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        KLTLogResponseDto dto = kltLogService.getMonthlyApiAndTrafficComparison(userDetails.getUser().getUserId());
        return ApiResponse.successOf(HttpStatus.OK, dto);
    }

    @GetMapping("/networkTrafficLastThreeMonths")
    @Operation(summary = "최근 3개월 동안의 네트워크 트래픽 사용량 조회 (Retrieve network traffic for the last three months)",
            description = "Returns the total network traffic for the user over the last three months.")
    public ApiResponse<Long> getNetworkTrafficForLastThreeMonths(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long traffic = kltLogService.getNetworkTrafficForLastThreeMonths(userDetails.getUser().getUserId());
        return ApiResponse.successOf(HttpStatus.OK, traffic);
    }

    @GetMapping("/apiCallsByMonth")
    @Operation(summary = "1월부터 12월까지 각 달마다 API 호출 횟수 조회 (Retrieve API calls for each month of the year)",
            description = "Returns the number of API calls for each month of the year for the user.")
    public ApiResponse<Map<Integer, Long>> getApiCallsCountByMonth(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<Integer, Long> monthlyApiCalls = kltLogService.getApiCallsCountByMonth(userDetails.getUser().getUserId());
        return ApiResponse.successOf(HttpStatus.OK, monthlyApiCalls);
    }

    @GetMapping("/networkTrafficByMonth")
    @Operation(summary = "1월부터 12월까지 각 달마다 네트워크 트래픽 사용량 조회 (Retrieve network traffic for each month of the year)",
            description = "Returns the network traffic for each month of the year for the user.")
    public ApiResponse<Map<Integer, Long>> getNetworkTrafficByMonth(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<Integer, Long> monthlyTraffic = kltLogService.getNetworkTrafficByMonth(userDetails.getUser().getUserId());
        return ApiResponse.successOf(HttpStatus.OK, monthlyTraffic);
    }

}
