package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    @EntityGraph(attributePaths = "logDetailSet")
    @Query("select a from ActivityLog a where a.userId = :userId AND a.status = 'Completed' ORDER BY a.id DESC")
    Page<ActivityLog> findActivityLogsByUserIdWithDetails(String userId, Pageable pageable);

    @EntityGraph(attributePaths = "logDetailSet")
    @Query("SELECT a FROM ActivityLog a WHERE a.userId = :userId AND a.modifiedAt >= :startDate AND a.status <> 'error' ORDER BY a.id DESC")
    Page<ActivityLog> findActivityLogsByUserIdAndDateRangeAndStatusNot(
           String userId, LocalDate startDate, Pageable pageable);

    @Query("select a.id from ActivityLog a where a.userId = :userId ORDER BY a.id DESC")
    Page<Long> findActivityLogIdsByUserId(String userId, Pageable pageable);

    @Query("select a from ActivityLog a join fetch a.logDetailSet where a.id in :ids")
    List<ActivityLog> findAllWithDetailsByIds(List<Long> ids);

    @Query("select count(a) from ActivityLog a where a.userId = :userId and FUNCTION('MONTH', a.createdAt) = :month")
    Long countActivityLogsByUserIdAndMonth(String userId, int month);

    @Query("select count(a) from ActivityLog a where a.userId = :userId")
    Long countAllActivityLogsByUserId(String userId);
}
