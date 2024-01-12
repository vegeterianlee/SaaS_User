package com.imcloud.saas_user.common.entity;

import com.imcloud.saas_user.common.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "payments")
public class Payment extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private int totalPrice;

    @Column
    private LocalDateTime paymentDate;

    @Column
    private String paymentMethod;

    @Column
    private String cardNumber;

    @Column
    private String cardUserName;

    @Column
    private String cardExpiredMonth;  // 01-12

    @Column
    private String cardExpiredYear;  // 년도 (2자리)

    @Column
    private String cardCvc;

    @Column
    private String country;

    // 삭제 플래그 (true: 삭제됨, false: 활성 상태)
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deletedFlag;

    // 삭제된 날짜
    @Column
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @OneToOne
    @JoinColumn(name = "subscriptionId")
    private Subscription subscription;


    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
}
