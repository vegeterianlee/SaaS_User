package com.imcloud.saas_user.common.entity;

import com.imcloud.saas_user.fileAction.dto.FileActionDto;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileActionSpecifications {

    // withDynamicQuery 메소드는 동적으로 쿼리를 생성하기 위한 Specification을 반환합니다.
    public static Specification<FileAction> withDynamicQuery(Boolean toBeDeidentified,
                                                             String fileName,
                                                             String objectKey,
                                                             LocalDateTime storedAtStart,
                                                             LocalDateTime storedAtEnd,
                                                             LocalDateTime isDeidentifiedAtStart,
                                                             LocalDateTime isDeidentifiedAtEnd) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // toBeDeidentified 값이 제공된 경우, 해당 조건으로 필터링합니다.
            if (toBeDeidentified != null) {
                predicates.add(cb.equal(root.get("toBeDeidentified"), toBeDeidentified));
            }
            // fileName 값이 제공된 경우, 해당 값으로 파일 이름 필터링(like 검색)합니다.
            if (fileName != null) {
                predicates.add(cb.like(root.get("fileName"), "%" + fileName + "%"));
            }
            // objectKey 값이 제공된 경우, 해당 값으로 객체 키 필터링(like 검색)합니다.
            if (objectKey != null) {
                predicates.add(cb.like(root.get("objectKey"), "%" + objectKey + "%"));
            }
            // storedAt의 시작과 종료 날짜가 모두 제공된 경우, 이 범위 내의 데이터로 필터링합니다.
            if (storedAtStart != null && storedAtEnd != null) {
                predicates.add(cb.between(root.get("storedAt"), storedAtStart, storedAtEnd));
            }
            // isDeidentifiedAt의 시작과 종료 날짜가 모두 제공된 경우, 이 범위 내의 데이터로 필터링합니다.
            if (isDeidentifiedAtStart != null && isDeidentifiedAtEnd != null) {
                predicates.add(cb.between(root.get("isDeidentifiedAt"), isDeidentifiedAtStart, isDeidentifiedAtEnd));
            }

            // 생성된 모든 조건(Predicate)들을 and 연산으로 결합하여 쿼리를 구성합니다.
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}


