package com.imcloud.saas_user.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "klt_logs")
public class KLTLog extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long kltApiCalls;

    @Column(nullable = false)
    private Long networkTraffic;  // Assuming in KB

    @Column
    private LocalDate lastApiCallDate;

    public static KLTLog create(String userId,  Long networkTraffic) {
        return KLTLog.builder()
                .userId(userId)
                .kltApiCalls(0L)
                .networkTraffic(networkTraffic)
                .lastApiCallDate(LocalDate.now())
                .build();
    }
}
