package com.imcloud.saas_user.common.entity;

import com.imcloud.saas_user.common.entity.enums.Product;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "subscriptions")
public class Subscription extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) //unique = true로 설정 시, 회원별로 오직 하나의 구독만 가능하다
    private Long memberId;

    @OneToOne(mappedBy = "subscription", cascade = CascadeType.ALL)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Product product;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean isActive;


    public void setIsActive(boolean isActive){
        this.isActive=isActive;
    }

    public void setEndDateNow(){
        this.endDate=LocalDateTime.now();
    }
    public void setEndDate(LocalDateTime endDate){
        this.endDate = endDate;
    }

}