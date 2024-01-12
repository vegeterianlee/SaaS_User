package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.FileAction;
import com.imcloud.saas_user.common.entity.StorageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface FileActionRepository extends JpaRepository<FileAction, Long>, JpaSpecificationExecutor<FileAction> {

    Optional<FileAction> findByObjectKey (String objectKey);
    Page<FileAction> findByUserIdAndIsDeidentifiedTarget(String userId, Boolean isDeidentifiedTarget, Pageable pageable);

    @Query("select f from FileAction f where f.userId = :userId " +
            "and f.storedAt >= :startDate")
    Page<FileAction> findByUserIdAndRecent(
            String userId, LocalDateTime startDate, Pageable pageable);
}
