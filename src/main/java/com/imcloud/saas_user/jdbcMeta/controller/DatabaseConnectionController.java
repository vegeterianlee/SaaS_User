package com.imcloud.saas_user.jdbcMeta.controller;

import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.dto.ErrorResponseDto;
import com.imcloud.saas_user.common.dto.ErrorType;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.jdbcMeta.dto.JdbcMetaDto;
import com.imcloud.saas_user.jdbcMeta.service.DatabaseConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.sql.SQLException;

import static com.imcloud.saas_user.common.dto.ErrorType.SQL_EXCEPTION;
import static com.imcloud.saas_user.common.dto.ErrorType.URI_SYNTAX_EXCEPTION;

@Tag(name = "Database Connection")
@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
public class DatabaseConnectionController {
    private final DatabaseConnectionService databaseConnectionService;

    @GetMapping("/testConnection")
    @Operation(summary = "데이터베이스 연결 테스트", description = "JDBC URL을 이용하여 데이터베이스 연결을 테스트합니다.")
    public ApiResponse<Boolean> testDatabaseConnection(
            @RequestParam String jdbcUrl,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        boolean connectionResult = databaseConnectionService.testConnection(userDetails, jdbcUrl);
        return ApiResponse.successOf(HttpStatus.OK, connectionResult);
    }

    @GetMapping("/exportData")
    @Operation(summary = "Export database data as CSV", description = "Exports data from the given JDBC URL as CSV.")
    public ApiResponse<?> exportDataAsCSV(
            @RequestParam String jdbcUrl,
            @RequestParam String table,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            String csvData = databaseConnectionService.exportDataAsCSV(userDetails, jdbcUrl, table);
            return ApiResponse.successOf(HttpStatus.OK, csvData);
        } catch (SQLException e) {
            ErrorResponseDto errorResponse = ErrorResponseDto.of(SQL_EXCEPTION, e.getMessage());
            return ApiResponse.failOf(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Create JDBC Meta Data", description = "JDBC URL과 테이블 이름을 사용하여 JdbcMeta 데이터를 생성합니다.")
    public ApiResponse<?> createJdbcMeta(
            @Parameter(hidden = true)@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String jdbcUrl,
            @RequestParam String table) {
        try {
            JdbcMetaDto jdbcMetaDto = databaseConnectionService.createJdbcMeta(userDetails, jdbcUrl, table);
            return ApiResponse.successOf(HttpStatus.OK, jdbcMetaDto);
        } catch (URISyntaxException e) {
            ErrorResponseDto errorResponse = ErrorResponseDto.of(URI_SYNTAX_EXCEPTION, e.getMessage());
            return ApiResponse.failOf(HttpStatus.BAD_REQUEST, errorResponse);
        }
    }

    @GetMapping("/getAll")
    @Operation(summary = "Get All JDBC Meta Data", description = "현재 사용자의 모든 JdbcMeta 데이터를 조회합니다.")
    public ApiResponse<Page<JdbcMetaDto>> getAllJdbcMetas(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<JdbcMetaDto> jdbcMetas = databaseConnectionService.getAllJdbcMetas(page, size, userDetails);
        return ApiResponse.successOf(HttpStatus.OK, jdbcMetas);
    }

    @GetMapping("/search")
    @Operation(summary = "Search JDBC Meta Data", description = "주어진 조건에 맞는 JdbcMeta 데이터를 조회합니다.")
    public ApiResponse<Page<JdbcMetaDto>> searchJdbcMetas(
            @Parameter(hidden = true)@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String jdbcUrl,
            @RequestParam(required = false) String table) {

        Page<JdbcMetaDto> jdbcMetas = databaseConnectionService.getJdbcMetas(page, size, userDetails, jdbcUrl, table);
        return ApiResponse.successOf(HttpStatus.OK, jdbcMetas);
    }

    @PutMapping("/update")
    @Operation(summary = "Update JDBC Meta Data", description = "기존의 JdbcMeta 데이터를 업데이트합니다.")
    public ApiResponse<JdbcMetaDto> updateJdbcMeta  (
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long id,
            @RequestParam String jdbcUrl,
            @RequestParam String table) throws URISyntaxException {
        JdbcMetaDto jdbcMetaDto = databaseConnectionService.updateJdbcMeta(userDetails, id, jdbcUrl, table);
        return ApiResponse.successOf(HttpStatus.OK, jdbcMetaDto);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete JDBC Meta Data", description = "JdbcMeta 데이터를 삭제합니다.")
    public ApiResponse<String> deleteJdbcMeta(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long id) {
        databaseConnectionService.deleteJdbcMeta(userDetails, id);
        return ApiResponse.successOf(HttpStatus.OK, "Jdbc 메타 정보 삭제완료");
    }


}
