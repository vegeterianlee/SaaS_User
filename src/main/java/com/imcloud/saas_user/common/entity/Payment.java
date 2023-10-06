package com.imcloud.saas_user.common.entity;

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
    private Long memberId;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false, length = 30)
    private String paymentMethod;

    @Column(nullable = false, length = 19)
    private String cardNumber;

    @Column(nullable = false, length = 15)
    private String cardUserName;

    @Column(nullable = false)
    private String cardExpiredMonth;  // 01-12

    @Column(nullable = false)
    private String cardExpiredYear;  // 년도 (2자리)

    @Column(nullable = false)
    private String cardCvv;

    @Column(nullable = false)
    private String country;

    @OneToOne
    @JoinColumn(name = "subscriptionId")
    private Subscription subscription;

}
