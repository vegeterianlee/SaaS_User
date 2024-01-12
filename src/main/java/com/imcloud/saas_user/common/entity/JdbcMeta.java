package com.imcloud.saas_user.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "jdbc_metas")
public class JdbcMeta extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jdbcName;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String serverUrl;

    @Column(nullable = false)
    private String userDatabase;

    @Column(nullable = false)
    private String hostName;

    @Column(nullable = false)
    private String dbPassword;

    @Column(nullable = false)
    private int portName;

    @Column(nullable = false)
    private String tableName;

    @Column(nullable = false)
    private String dbUser;

    // 삭제 플래그 (true: 삭제됨, false: 활성 상태)
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deletedFlag;

    // 삭제된 날짜
    @Column
    private LocalDateTime deletedAt;

    public static JdbcMeta create(String jdbcName, String serverUrl, String userId, String database, String host,
                                  String dbPassword, int port, String table, String dbUser) {
        return JdbcMeta.builder()
                .jdbcName(jdbcName)
                .serverUrl(serverUrl)
                .userId(userId)
                .userDatabase(database)
                .hostName(host)
                .dbPassword(dbPassword)
                .portName(port)
                .tableName(table)
                .dbUser(dbUser)
                .deletedFlag(false)
                .build();
    }
}
