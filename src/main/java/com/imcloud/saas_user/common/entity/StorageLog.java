package com.imcloud.saas_user.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.imcloud.saas_user.common.entity.enums.FileActionType;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

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

    @Column
    private String fileName;

    @Column(nullable = false)
    private String objectKey;


    @Column(nullable = false)
    private Boolean isPaid;

    @Column(nullable = false)
    private Long networkTraffic;  // Assuming in KB

    @Column(nullable = false)
    private LocalDateTime storedAt;

    public static StorageLog create(String userId, String fileName, Long networkTraffic, String objectKey) {
        return StorageLog.builder()
                .userId(userId)
                .networkTraffic(networkTraffic)
                .isPaid(false)
                .fileName(fileName)
                .objectKey(objectKey)
                .storedAt(LocalDateTime.now())
                .build();
    }
}
