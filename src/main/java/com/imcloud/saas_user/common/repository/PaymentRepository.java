package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.Payment;
import com.imcloud.saas_user.common.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM payments p INNER JOIN p.subscription s WHERE p.userId = :userId ORDER BY p.id DESC")
    @EntityGraph(attributePaths = {
            "subscription"
    })
    Page<Payment> findPaymentDetailsByUserId(String userId, Pageable pageable);

    // userId와 PaymentStatus를 기반으로 Payment 객체 조회
    Payment findByUserIdAndPaymentStatus(String userId, PaymentStatus paymentStatus);

    Payment findByUserIdAndPaymentStatusAndProductName(String userId, PaymentStatus paymentStatus, String productName);
}
