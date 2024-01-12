package com.imcloud.saas_user.jdbcMeta.dto;

import com.imcloud.saas_user.common.entity.JdbcMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class JdbcMetaDto {
    @Schema(type = "integer", example = "1", description = "메타 데이터 ID")
    private Long id;

    @Schema(type = "string", example = "jdbc:mysql://host:pw@localhost:3306/database", description = "서버 URL")
    private String serverUrl;

    @Schema(type = "string", example = "user123", description = "사용자 ID")
    private String userId;

    @Schema(type = "string", example = "database_name", description = "데이터베이스 이름")
    private String database;

    @Schema(type = "string", example = "example.host.com", description = "데이터베이스 호스트")
    private String host;

    @Schema(type = "string", example = "password123", description = "데이터베이스 비밀번호")
    private String dbPassword;

    @Schema(type = "string", example = "12345", description = "데이터베이스 포트")
    private int port;

    @Schema(type = "string", example = "example_table", description = "테이블 이름")
    private String table;

    @Schema(type = "string", example = "db_user", description = "데이터베이스 사용자")
    private String dbUser;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static JdbcMetaDto of(JdbcMeta jdbcMeta) {
        return JdbcMetaDto.builder()
                .id(jdbcMeta.getId())
                .serverUrl(jdbcMeta.getServerUrl())
                .userId(jdbcMeta.getUserId())
                .database(jdbcMeta.getUserDatabase())
                .host(jdbcMeta.getHostName())
                .dbPassword(jdbcMeta.getDbPassword())
                .port(jdbcMeta.getPortName())
                .table(jdbcMeta.getTableName())
                .dbUser(jdbcMeta.getDbUser())
                .createdAt(jdbcMeta.getCreatedAt())
                .modifiedAt(jdbcMeta.getModifiedAt())
                .build();
    }
}
