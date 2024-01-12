package com.imcloud.saas_user.common.entity;

import com.imcloud.saas_user.common.entity.JdbcMeta;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class JdbcMetaSpecifications {

    public static Specification<JdbcMeta> withDynamicQuery(String userId,
                                                           String jdbcName,
                                                           String jdbcUrl,
                                                           String tableName) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // userId 조건이 제공된 경우, 해당 조건으로 필터링합니다.
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            // jdbcName 조건이 제공된 경우, 해당 값으로 서버 URL 필터링(like 검색)합니다.
            if (jdbcName != null) {
                predicates.add(cb.like(root.get("jdbcName"), "%" + jdbcName + "%"));
            }

            // jdbcUrl 조건이 제공된 경우, 해당 값으로 서버 URL 필터링(like 검색)합니다.
            if (jdbcUrl != null) {
                predicates.add(cb.like(root.get("serverUrl"), "%" + jdbcUrl + "%"));
            }
            // table 조건이 제공된 경우, 해당 값으로 테이블 이름 필터링(like 검색)합니다.
            if (tableName != null) {
                predicates.add(cb.like(root.get("tableName"), "%" + tableName + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

