package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    @Query("select a from ActivityLog a where a.userId = :userId ORDER BY a.id DESC")
    Page<ActivityLog> findActivityLogsByUserId(String userId, Pageable pageable);
}
