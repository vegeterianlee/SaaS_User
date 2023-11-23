package com.imcloud.saas_user.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "storage_logs")
public class StorageLog extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String objectKey;

    @Column(nullable = false)
    private Boolean isPaid;

    @Column(nullable = false)
    private Long networkTraffic;  // Assuming in KB

    @Column(nullable = false)
    private LocalDateTime storedAt;

    public static StorageLog create(String userId, Long networkTraffic, String objectKey) {
        return StorageLog.builder()
                .userId(userId)
                .networkTraffic(networkTraffic)
                .isPaid(false)
                .objectKey(objectKey)
                .storedAt(LocalDateTime.now())
                .build();
    }
}
