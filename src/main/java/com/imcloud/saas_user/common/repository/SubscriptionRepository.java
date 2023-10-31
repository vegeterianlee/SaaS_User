package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription>findByUserId(String userId);
    List<Subscription>findAllByUserId(String userId);
    Optional<Subscription> findByUserIdAndIsActive(String userId, Boolean isActive);
    List<Subscription> findAllByEndDateBeforeAndIsActive(LocalDateTime endDate, Boolean isActive);
}

