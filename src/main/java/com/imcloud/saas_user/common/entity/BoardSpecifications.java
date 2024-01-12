package com.imcloud.saas_user.common.entity;

import com.imcloud.saas_user.common.entity.enums.UserRole;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class BoardSpecifications {

    public static Specification<Board> withDynamicQuery(Boolean hasAdminComment,
                                                        String title,
                                                        String content,
                                                        String userRole) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasAdminComment != null) {
                predicates.add(cb.equal(root.get("hasAdminComment"), hasAdminComment));
            }

            if (title != null) {
                predicates.add(cb.like(root.get("title"), "%" + title + "%"));
            }

            if (content != null) {
                predicates.add(cb.like(root.get("content"), "%" + content + "%"));
            }

            if (userRole != null) {
                UserRole enumRole = UserRole.fromString(userRole);
                predicates.add(cb.equal(root.get("member").get("role"), enumRole));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
