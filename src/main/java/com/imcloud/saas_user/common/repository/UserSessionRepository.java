package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    @Query("select count(s) from sessions s where s.userId = :userId and s.userStatus = true and s.expirationTime > :now")
    int countActiveSessionsByMember(String userId, LocalDateTime now);

    @Query("select count(s) from sessions s where s.userId = :userId and s.userStatus = true")
    int countActiveSessionsByMember(String userId);
    List<UserSession> findByExpirationTimeBeforeAndUserStatus(LocalDateTime expirationTime, boolean userStatus);

    List<UserSession> findByUserIdAndIpAddressAndUserStatus(String userId, String ipAddress, boolean userStatus);

}
