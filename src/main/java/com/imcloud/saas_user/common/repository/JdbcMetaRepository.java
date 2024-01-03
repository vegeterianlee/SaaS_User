package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.JdbcMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JdbcMetaRepository  extends JpaRepository<JdbcMeta, Long>, JpaSpecificationExecutor<JdbcMeta> {
    // 사용자 ID, 서버 URL, 테이블 이름을 기준으로 JdbcMeta 객체를 검색합니다.
    Optional<JdbcMeta> findByUserIdAndServerUrlAndTableName(String userId, String serverUrl, String table);

    // ID 및 사용자 ID를 기준으로 JdbcMeta 객체를 검색합니다.
    Optional<JdbcMeta> findByIdAndUserId(Long id, String userId);

    Page<JdbcMeta> findByUserId(String userId, Pageable pageable);

}
