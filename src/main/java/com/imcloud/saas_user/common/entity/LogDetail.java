package com.imcloud.saas_user.common.entity;

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
@Table(name = "log_details")
public class LogDetail extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_method", nullable = true, length = 200)
    private String dataMethod;

    @Column(name = "column_name", nullable = true, length = 200)
    private String columnName;

    @Column(name = "data_type", nullable = true, length = 200)
    private String dataType;

    @Column(name = "description", nullable = true, length = 200)
    private String description;

    @Column(name = "data_size", nullable = true)
    private Long dataSize;

    // 삭제 플래그 (true: 삭제됨, false: 활성 상태)
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deletedFlag;

    // 삭제된 날짜
    @Column
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name="activity_id")
    private ActivityLog activityLog;
}
