package com.imcloud.saas_user.common.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "activity_logs")
public class ActivityLog extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = true)
    private String fileName;

    @Column(nullable = false, columnDefinition = "varchar(10) default '0'")
    private String processingTime;

    @Column(nullable = false, columnDefinition = "varchar(50) default 'Processing'")
    private String status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "activityLog", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<LogDetail> logDetailSet;

}
