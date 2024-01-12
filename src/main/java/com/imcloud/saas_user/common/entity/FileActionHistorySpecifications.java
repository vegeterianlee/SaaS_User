package com.imcloud.saas_user.common.entity;

import com.imcloud.saas_user.common.entity.FileActionHistory;
import com.imcloud.saas_user.common.entity.enums.FileActionType;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileActionHistorySpecifications {

    // 동적 쿼리를 위한 Specification을 생성하는 정적 메소드
    public static Specification<FileActionHistory> createSpecification(
            String fileName, // 파일 이름
            String userId,
            String objectKey, // 객체 키
            FileActionType actionType, // 파일 액션 타입
            LocalDateTime actionTimeStart, // 액션 시작 시간
            LocalDateTime actionTimeEnd) { // 액션 종료 시간

        return (root, query, cb) -> { // 쿼리를 구성하기 위한 람다 표현식입니다.
            List<Predicate> predicates = new ArrayList<>();

            // userId 조건을 강제로 추가합니다.
            predicates.add(cb.equal(root.get("userId"), userId));

            if (fileName != null && !fileName.isEmpty()) {
                predicates.add(cb.like(root.get("fileName"), "%" + fileName + "%")); // 파일 이름에 대한 like 검색 조건을 추가합니다.
            }
            if (objectKey != null && !objectKey.isEmpty()) {
                predicates.add(cb.like(root.get("objectKey"), "%" + objectKey + "%")); // 객체 키에 대한 like 검색 조건을 추가합니다.
            }
            if (actionType != null) {
                predicates.add(cb.equal(root.get("actionType"), actionType)); // 파일 액션 타입에 대한 동등성 검사 조건을 추가합니다.
            }
            if (actionTimeStart != null && actionTimeEnd != null) {
                predicates.add(cb.between(root.get("actionTime"), actionTimeStart, actionTimeEnd)); // 액션 시간 범위에 대한 조건을 추가합니다.
            }

            return cb.and(predicates.toArray(new Predicate[0])); // 모든 조건을 and 연산으로 결합합니다.
        };
    }
}
