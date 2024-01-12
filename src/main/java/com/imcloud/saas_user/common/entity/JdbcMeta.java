package com.imcloud.saas_user.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import lombok.*;


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

    public static JdbcMeta create(String serverUrl, String userId, String database, String host,
                                  String dbPassword, int port, String table, String dbUser) {
        return JdbcMeta.builder()
                .serverUrl(serverUrl)
                .userId(userId)
                .userDatabase(database)
                .hostName(host)
                .dbPassword(dbPassword)
                .portName(port)
                .tableName(table)
                .dbUser(dbUser)
                .build();
    }
}
