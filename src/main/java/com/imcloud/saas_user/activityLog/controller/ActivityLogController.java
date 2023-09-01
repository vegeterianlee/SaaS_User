package com.imcloud.saas_user.activityLog.controller;

import com.imcloud.saas_user.activityLog.dto.ActivityLogResponseDto;
import com.imcloud.saas_user.activityLog.service.ActivityLogService;
import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Processing History")
@RestController
@RequestMapping("/api/activitylogs")
@RequiredArgsConstructor
public class ActivityLogController {
    private final ActivityLogService activityLogService;

    @GetMapping
    @Operation(summary = "사용자의 비식별 처리 히스토리 조회 (get user's Processing history)", description = "Page starts with 1, sort in descending order")
    public ApiResponse<Page<ActivityLogResponseDto>> getActivityLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.OK, activityLogService.getActivityLogs(page, size, userDetails));
    }
    @GetMapping("/monthly")
    @Operation(summary = "사용자의 월별 비식별 처리 히스토리 수 조회 (get user's monthly Processing history counts)")
    public ApiResponse<Map<Integer, Long>> getMonthlyActivityLogs(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.OK, activityLogService.getActivityLogsCountByMonth(userDetails));
    }

    @GetMapping("/totalCount")
    @Operation(summary = "사용자의 전체 비식별 처리 히스토리 수 조회 (get user's total Processing history counts)")
    public ApiResponse<Long> getTotalActivityLogs(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.OK, activityLogService.getAllActivityLogsCount(userDetails));
    }
}
