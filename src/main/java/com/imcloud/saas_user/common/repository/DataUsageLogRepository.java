package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.DataUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DataUsageLogRepository extends JpaRepository<DataUsageLog, Long> {

    List<DataUsageLog> findByUserIdAndUsedAtAfter(String userId, LocalDateTime date);
    List<DataUsageLog> findByUserIdAndUsedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
}
