package com.imcloud.saas_user.dataUsage.controller;

import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.dataUsage.dto.DataUsageResponseDto;
import com.imcloud.saas_user.dataUsage.service.DataUsageService;
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

@Tag(name = "DataUsage")
@RestController
@RequestMapping("/api/dataUsage")
@RequiredArgsConstructor
public class DataUsageController {
    private final DataUsageService dataUsageService;

    @GetMapping("/dataUsageBreakdown")
    @Operation(
            summary = "최근 3개월 간의 데이터 사용량 분석 (Retrieve data usage breakdown for the last three months)",
            description = "Returns the data usage breakdown for each of the last three months for the user."
    )
    public ApiResponse<Map<String, Long>> getDataUsageForLastThreeMonths(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, Long> usageBreakdown = dataUsageService.getDataUsageForLastThreeMonths(userDetails);
        return ApiResponse.successOf(HttpStatus.OK, usageBreakdown);
    }

    @GetMapping("/threeMonthsUsage")
    @Operation(summary = "최근 3개월 동안의 데이터 사용량 조회 (Retrieve data usage for the last three months)",
            description = "Returns the total data usage for the user over the last three months.")
    public ApiResponse<Long> getDataUsageLastThreeMonths(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long usage = dataUsageService.getDataUsageLastThreeMonths(userDetails);
        return ApiResponse.successOf(HttpStatus.OK, usage);
    }

    @GetMapping("/monthlyComparison")
    @Operation(summary = "저번달과 이번달의 데이터 사용량 비교 (Compare data usage between last month and this month)",
            description = "Returns the data usage comparison between the current month and the previous month for the user.")
    public ApiResponse<DataUsageResponseDto> getMonthlyDataComparison(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DataUsageResponseDto dto = dataUsageService.getMonthlyDataComparison(userDetails);
        return ApiResponse.successOf(HttpStatus.OK, dto);
    }
}
