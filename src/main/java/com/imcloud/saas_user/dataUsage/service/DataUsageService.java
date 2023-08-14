package com.imcloud.saas_user.dataUsage.service;

import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.DataUsageLog;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.repository.DataUsageLogRepository;
import com.imcloud.saas_user.common.repository.MemberRepository;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.dataUsage.dto.DataUsageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class DataUsageService {
    private final DataUsageLogRepository dataUsageLogRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Map<String, Long> getDataUsageForLastThreeMonths(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        Map<String, Long> monthlyDataUsage = new LinkedHashMap<>(); // To maintain order

        for (int i = 2; i >= 0; i--) {
            LocalDateTime startOfMonth = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

            List<DataUsageLog> logs = dataUsageLogRepository.findByUserIdAndUsedAtBetween(member.getUserId(), startOfMonth, endOfMonth);
            Long usage = logs.stream().mapToLong(DataUsageLog::getDataSize).sum();

            monthlyDataUsage.put(startOfMonth.getMonth().toString(), usage);
        }

        return monthlyDataUsage;
    }

    @Transactional(readOnly = true)
    public Long getDataUsageLastThreeMonths(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        List<DataUsageLog> logs = dataUsageLogRepository.findByUserIdAndUsedAtAfter(member.getUserId(), threeMonthsAgo);

        return logs.stream().mapToLong(DataUsageLog::getDataSize).sum();
    }

    @Transactional(readOnly = true)
    public DataUsageResponseDto getMonthlyDataComparison(UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        // 현재 달의 시작과 끝 날짜
        LocalDateTime startOfCurrentMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfCurrentMonth = startOfCurrentMonth.plusMonths(1).minusSeconds(1);

        // 이전 달의 시작과 끝 날짜
        LocalDateTime startOfLastMonth = startOfCurrentMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfCurrentMonth.minusSeconds(1);

        // 이전 달의 데이터 사용량
        List<DataUsageLog> logsOfLastMonth = dataUsageLogRepository.findByUserIdAndUsedAtBetween(member.getUserId(), startOfLastMonth, endOfLastMonth);
        Long lastMonthUsage = logsOfLastMonth.stream().mapToLong(DataUsageLog::getDataSize).sum();

        // 현재 달의 데이터 사용량
        List<DataUsageLog> logsOfCurrentMonth = dataUsageLogRepository.findByUserIdAndUsedAtBetween(member.getUserId(), startOfCurrentMonth, endOfCurrentMonth);
        Long currentMonthUsage = logsOfCurrentMonth.stream().mapToLong(DataUsageLog::getDataSize).sum();

        // 저번 달 대비 현재 달의 사용량 퍼센트 계산
        Double usagePercentageIncrease;
        if (lastMonthUsage == 0) {
            // 지난달 사용량이 0인 경우 100%로 설정
            usagePercentageIncrease = 100.0;
        } else {
            // 저번 달 대비 현재 달의 사용량 퍼센트 계산
            usagePercentageIncrease = (double) (currentMonthUsage - lastMonthUsage) / lastMonthUsage * 100;
        }

        // DTO에 값을 할당하고 반환
        return DataUsageResponseDto.of(currentMonthUsage, lastMonthUsage, usagePercentageIncrease);
    }

}
