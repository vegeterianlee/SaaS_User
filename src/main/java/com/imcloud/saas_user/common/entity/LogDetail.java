package com.imcloud.saas_user.common.entity;

import lombok.*;

import javax.persistence.*;
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

    @Column(name = "data_method", nullable = false, length = 200)
    private String dataMethod;

    @Column(name = "column_name", nullable = false, length = 200)
    private String columnName;

    @Column(name = "data_type", nullable = false, length = 200)
    private String dataType;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Column(name = "data_size", nullable = false)
    private int dataSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="activity_id")
    private ActivityLog activityLog;
}
