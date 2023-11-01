package com.imcloud.saas_user.kltLog.service;

import com.imcloud.saas_user.common.entity.KLTLog;
import com.imcloud.saas_user.common.repository.KLTLogRepository;
import com.imcloud.saas_user.kltLog.dto.KLTLogResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KLTLogService {

    private final KLTLogRepository kltLogRepository;

    @Transactional(readOnly = true)
    public Map<String, Long> getApiCallsForLastThreeMonths(String userId) {
        Map<String, Long> monthlyApiCalls = new HashMap<>();

        for (int i = 0; i < 3; i++) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDate startOfMonth = ym.atDay(1);
            LocalDate endOfMonth = ym.atEndOfMonth();

            List<KLTLog> logs = kltLogRepository.findByUserIdAndLastApiCallDateBetween(userId, startOfMonth, endOfMonth);
            Long totalCalls = logs.stream().mapToLong(KLTLog::getKltApiCalls).sum();

            monthlyApiCalls.put(startOfMonth.getMonth().toString(), totalCalls);
        }

        return monthlyApiCalls;
    }

    @Transactional(readOnly = true)
    public Long getTotalNetworkTrafficForMonth(String userId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        List<KLTLog> logs = kltLogRepository.findByUserIdAndLastApiCallDateBetween(userId, startOfMonth, endOfMonth);
        return logs.stream().mapToLong(KLTLog::getNetworkTraffic).sum();
    }

    @Transactional(readOnly = true)
    public Long getTotalApiCallsForMonth(String userId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        List<KLTLog> logs = kltLogRepository.findByUserIdAndLastApiCallDateBetween(userId, startOfMonth, endOfMonth);
        return logs.stream().mapToLong(KLTLog::getKltApiCalls).sum();
    }

    @Transactional
    public void resetMonthlyApiCalls(String userId) {
        List<KLTLog> logs = kltLogRepository.findByUserId(userId);
        for (KLTLog log : logs) {
            log.setKltApiCalls(0L);
        }
        kltLogRepository.saveAll(logs);
    }

    @Transactional(readOnly = true)
    public KLTLogResponseDto getMonthlyApiAndTrafficComparison(String userId) {
        LocalDate startOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfCurrentMonth = startOfCurrentMonth.plusMonths(1).minusDays(1);

        LocalDate startOfLastMonth = startOfCurrentMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfCurrentMonth.minusDays(1);

        // API Calls for current and last month
        Long currentMonthApiCalls = kltLogRepository.findByUserIdAndLastApiCallDateBetween(userId, startOfCurrentMonth, endOfCurrentMonth)
                .stream()
                .mapToLong(KLTLog::getKltApiCalls)
                .sum();

        Long lastMonthApiCalls = kltLogRepository.findByUserIdAndLastApiCallDateBetween(userId, startOfLastMonth, endOfLastMonth)
                .stream()
                .mapToLong(KLTLog::getKltApiCalls)
                .sum();

        Double apiCallsPercentageIncrease;
        if (lastMonthApiCalls == 0) {
            apiCallsPercentageIncrease = 100.0;
        } else {
            apiCallsPercentageIncrease = (double) (currentMonthApiCalls - lastMonthApiCalls) / lastMonthApiCalls * 100;
        }

        // Network Traffic for current and last month
        Long currentMonthTraffic = kltLogRepository.findByUserIdAndLastApiCallDateBetween(userId, startOfCurrentMonth, endOfCurrentMonth)
                .stream()
                .mapToLong(KLTLog::getNetworkTraffic)
                .sum();

        Long lastMonthTraffic = kltLogRepository.findByUserIdAndLastApiCallDateBetween(userId, startOfLastMonth, endOfLastMonth)
                .stream()
                .mapToLong(KLTLog::getNetworkTraffic)
                .sum();

        Double trafficPercentageIncrease;
        if (lastMonthTraffic == 0) {
            trafficPercentageIncrease = 100.0;
        } else {
            trafficPercentageIncrease = (double) (currentMonthTraffic - lastMonthTraffic) / lastMonthTraffic * 100;
        }

        return KLTLogResponseDto.of(currentMonthApiCalls, lastMonthApiCalls, apiCallsPercentageIncrease,
                currentMonthTraffic, lastMonthTraffic, trafficPercentageIncrease);
    }

    @Transactional(readOnly = true)
    public Long getNetworkTrafficForLastThreeMonths(String userId) {
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        List<KLTLog> logs = kltLogRepository.findByUserIdAndLastApiCallDateAfter(userId, threeMonthsAgo);
        return logs.stream().mapToLong(KLTLog::getNetworkTraffic).sum();
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> getApiCallsCountByMonth(String userId) {
        Map<Integer, Long> monthlyApiCalls = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            Long count = kltLogRepository.countApiCallsByUserIdAndMonth(userId, month);
            monthlyApiCalls.put(month, count);
        }
        return monthlyApiCalls;
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> getNetworkTrafficByMonth(String userId) {
        Map<Integer, Long> monthlyTraffic = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            Long traffic = kltLogRepository.countNetworkTrafficByUserIdAndMonth(userId, month);
            monthlyTraffic.put(month, traffic);
        }
        return monthlyTraffic;
    }
}
