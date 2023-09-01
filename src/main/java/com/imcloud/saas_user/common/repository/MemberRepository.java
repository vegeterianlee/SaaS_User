package com.imcloud.saas_user.common.repository;



import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.entity.UserSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUsername(String username);
    Optional<Member> findByUserId(String userId);

    @Query("select u from sessions u where u.userId = :userId order by u.loginTime desc")
    List<UserSession> findLatestSessionsByUserId(String userId, Pageable pageable);

}
