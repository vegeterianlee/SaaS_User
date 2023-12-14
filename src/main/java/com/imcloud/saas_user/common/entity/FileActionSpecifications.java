package com.imcloud.saas_user.common.entity;

import com.imcloud.saas_user.fileAction.dto.FileActionDto;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileActionSpecifications {

    public static Specification<FileAction> withDynamicQuery(Boolean toBeDeidentified,
                                                             String fileName,
                                                             String objectKey,
                                                             LocalDateTime storedAtStart,
                                                             LocalDateTime storedAtEnd,
                                                             LocalDateTime isDeidentifiedAtStart,
                                                             LocalDateTime isDeidentifiedAtEnd) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (toBeDeidentified != null) {
                predicates.add(cb.equal(root.get("toBeDeidentified"), toBeDeidentified));
            }
            if (fileName != null) {
                predicates.add(cb.like(root.get("fileName"), "%" + fileName + "%"));
            }
            if (objectKey != null) {
                predicates.add(cb.like(root.get("objectKey"), "%" + objectKey + "%"));
            }
            if (storedAtStart != null && storedAtEnd != null) {
                predicates.add(cb.between(root.get("storedAt"), storedAtStart, storedAtEnd));
            }
            if (isDeidentifiedAtStart != null && isDeidentifiedAtEnd != null) {
                predicates.add(cb.between(root.get("isDeidentifiedAt"), isDeidentifiedAtStart, isDeidentifiedAtEnd));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}


