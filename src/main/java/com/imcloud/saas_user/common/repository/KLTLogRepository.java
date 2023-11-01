package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.KLTLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KLTLogRepository extends JpaRepository<KLTLog, Long> {

    List<KLTLog> findByUserIdAndLastApiCallDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    List<KLTLog> findByUserIdAndLastApiCallDateAfter(String userId, LocalDate date);

    @Query("SELECT SUM(k.kltApiCalls) FROM KLTLog k WHERE k.userId = :userId AND FUNCTION('MONTH', k.lastApiCallDate) = :month")
    Long countApiCallsByUserIdAndMonth(String userId, int month);

    @Query("SELECT SUM(k.networkTraffic) FROM KLTLog k WHERE k.userId = :userId AND FUNCTION('MONTH', k.lastApiCallDate) = :month")
    Long countNetworkTrafficByUserIdAndMonth(String userId, int month);


    List<KLTLog> findByUserId(String userId);
}
