package com.imcloud.saas_user.jdbcMeta.service;

import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.JdbcMeta;
import com.imcloud.saas_user.common.entity.JdbcMetaSpecifications;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.repository.JdbcMetaRepository;
import com.imcloud.saas_user.common.repository.MemberRepository;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.jdbcMeta.dto.JdbcMetaDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.sql.DataSource;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

@Service
@RequiredArgsConstructor
public class DatabaseConnectionService {
    private final MemberRepository memberRepository;
    private final JdbcMetaRepository jdbcMetaRepository;

    public boolean testConnection(UserDetailsImpl userDetails, String jdbcUrl) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.MEMBER_NOT_FOUND.getMessage())
        );

        try {
            // DB 연결을 시도합니다.
            try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
                // 연결에 성공하면 true 반환
                return true;
            }
        } catch (SQLException e) {
            // 연결에 실패하면 false 반환
            return false;
        }
    }

    public String exportDataAsCSV(UserDetailsImpl userDetails, String jdbcUrl, String table) throws SQLException {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.MEMBER_NOT_FOUND.getMessage())
        );

        String query = String.format("SELECT * FROM %s LIMIT 500", table);

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            StringWriter stringWriter = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT.withHeader(resultSet));

            csvPrinter.printRecords(resultSet);
            csvPrinter.flush();

            return stringWriter.toString();
        } catch (SQLException e) {
            throw new SQLException("Failed to export data as CSV", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JdbcMetaDto createJdbcMeta(UserDetailsImpl userDetails, String jdbcUrl, String table) throws URISyntaxException, URISyntaxException {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.MEMBER_NOT_FOUND.getMessage())
        );

        // JDBC URL을 파싱하여 필요한 정보를 추출합니다.
        URI jdbcUri = new URI(jdbcUrl.substring(5)); // "jdbc:" 부분을 제거합니다.

        String userInfo = jdbcUri.getUserInfo();
        if (userInfo == null) {
            throw new IllegalArgumentException("JDBC URL이 올바르지 않습니다.");
        }

        String[] userParts = userInfo.split(":", 2);
        String dbUser = userParts[0];
        String dbPassword = userParts[1];

        String host = jdbcUri.getHost();
        int port = jdbcUri.getPort(); // 포트는 int 타입으로 파싱됩니다.

        // 데이터베이스 이름은 경로의 첫 번째 '/' 이후의 부분입니다.
        String path = jdbcUri.getPath();
        String database = path.startsWith("/") ? path.substring(1) : path;

        // UserDetailsImpl에서 사용자 ID를 가져옵니다.
        String userId = member.getUserId();

        // JdbcMeta 객체를 생성하고 저장합니다.
        JdbcMeta jdbcMeta = JdbcMeta.create(jdbcUrl, userId, database, host, dbPassword, port, table, dbUser);
        jdbcMeta = jdbcMetaRepository.save(jdbcMeta);
        return JdbcMetaDto.of(jdbcMeta);
    }

    @Transactional(readOnly = true)
    public Page<JdbcMetaDto> getAllJdbcMetas(Integer page, Integer size, UserDetailsImpl userDetails) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.MEMBER_NOT_FOUND.getMessage())
        );

        String userId = member.getUserId();
        Pageable pageable = PageRequest.of(page - 1, size);

        // JdbcMeta 객체를 조회하고, DTO로 변환합니다.
        return jdbcMetaRepository.findByUserId(userId, pageable)
                .map(JdbcMetaDto::of);
    }

    @Transactional(readOnly = true)
    public Page<JdbcMetaDto> getJdbcMetas(Integer page, Integer size, UserDetailsImpl userDetails,
                                          String jdbcUrl, String table) {

        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.MEMBER_NOT_FOUND.getMessage())
        );

        String userId = member.getUserId();
        Pageable pageable = PageRequest.of(page - 1, size);

        // 동적 쿼리를 생성합니다.
        Specification<JdbcMeta> spec = JdbcMetaSpecifications.withDynamicQuery(userId, jdbcUrl, table);

        // JdbcMeta 객체를 조회하고, DTO로 변환합니다.
        return jdbcMetaRepository.findAll(spec, pageable).map(JdbcMetaDto::of);
    }


    @Transactional
    public JdbcMetaDto updateJdbcMeta (UserDetailsImpl userDetails, String jdbcUrl, String table) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.MEMBER_NOT_FOUND.getMessage())
        );

        // 저장된 JdbcMeta 객체를 찾습니다.
        JdbcMeta jdbcMeta = jdbcMetaRepository.findByUserIdAndServerUrlAndTableName(member.getUserId(), jdbcUrl, table)
                .orElseThrow(() -> new RuntimeException("JdbcMeta 객체를 찾을 수 없습니다."));

        // 찾은 객체를 업데이트합니다.
        jdbcMeta.setServerUrl(jdbcUrl);
        jdbcMeta.setTableName(table);
        // 기타 필요한 컬럼들도 업데이트할 수 있습니다.

        jdbcMeta = jdbcMetaRepository.save(jdbcMeta);
        return JdbcMetaDto.of(jdbcMeta);
    }

    @Transactional
    public void deleteJdbcMeta(UserDetailsImpl userDetails, Long id) {
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.MEMBER_NOT_FOUND.getMessage())
        );

        // JdbcMeta 객체를 찾습니다.
        JdbcMeta jdbcMeta = jdbcMetaRepository.findByIdAndUserId(id, userDetails.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("JdbcMeta 객체를 찾을 수 없습니다."));

        // 찾은 객체를 삭제합니다.
        jdbcMetaRepository.delete(jdbcMeta);
    }








}
