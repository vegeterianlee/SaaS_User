package com.imcloud.saas_user.common.repository;
import com.imcloud.saas_user.common.entity.FileAction;
import com.imcloud.saas_user.common.entity.FileActionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileActionHistoryRepository extends JpaRepository<FileActionHistory, Long>, JpaSpecificationExecutor<FileActionHistory> {

    Optional<FileActionHistory> findByFileActionId(Long fileActionId);

}
