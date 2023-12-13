package com.imcloud.saas_user.activityLog.service;

import com.imcloud.saas_user.activityLog.dto.ActivityLogResponseDto;
import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.ActivityLog;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.repository.ActivityLogRepository;
import com.imcloud.saas_user.common.repository.MemberRepository;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final MemberRepository memberRepository;
    private final ActivityLogRepository activityLogRepository;

    @Transactional(readOnly = true)
    public Page<ActivityLogResponseDto> getActivityLogs(Integer page, Integer size, UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        Pageable pageable = PageRequest.of(page-1, size);
        Page<ActivityLog> logs = activityLogRepository.findActivityLogsByUserIdWithDetails(member.getUserId(), pageable);

        return logs.map(this::toDto);
    }

    // 추가된 private 메소드, of 정적 메서드로 호출해도 상관없음
    private ActivityLogResponseDto toDto(ActivityLog activityLog) {
        return ActivityLogResponseDto.of(activityLog);
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponseDto> getActivityLogsBy7days(Integer page, Integer size, UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        Pageable pageable = PageRequest.of(page-1, size);

        // 현재 날짜로부터 7일 전까지의 날짜 계산
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        // 수정된 레포지토리 메서드 호출
        Page<ActivityLog> logs = activityLogRepository.findActivityLogsByUserIdAndDateRangeAndStatusNot(
                member.getUserId(), sevenDaysAgo, pageable);

        return logs.map(this::toDto);
    }


    @Transactional(readOnly = true)
    public Map<Integer, Long> getActivityLogsCountByMonth(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage()));

        Map<Integer, Long> monthlyLogsCount = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            Long count = activityLogRepository.countActivityLogsByUserIdAndMonth(member.getUserId(), month);
            monthlyLogsCount.put(month, count);
        }

        return monthlyLogsCount;
    }

    @Transactional(readOnly = true)
    public Long getAllActivityLogsCount(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage()));

        return activityLogRepository.countAllActivityLogsByUserId(member.getUserId());
    }
}
