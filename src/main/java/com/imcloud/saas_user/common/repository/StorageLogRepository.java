package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.StorageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StorageLogRepository extends JpaRepository<StorageLog, Long> {
    Optional<StorageLog> findByObjectKey(String objectKey);
    List<StorageLog> findAllByUserId(String userId);
}
