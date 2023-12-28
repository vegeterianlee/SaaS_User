package com.imcloud.saas_user.objStorage.dto;

import com.imcloud.saas_user.common.entity.StorageLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StorageLogResponseDto {

    @Schema(type = "integer", example = "1", description = "로그 ID")
    private Long id;

    @Schema(type = "string", example = "user123", description = "사용자 ID")
    private String userId;

    @Schema(type = "string", example = "document.pdf", description = "파일 이름")
    private String fileName;

    @Schema(type = "string", example = "de-identification/folder/user123/document.pdf", description = "S3 버킷 내 객체 키")
    private String objectKey;


    @Schema(type = "boolean", example = "false", description = "결제 여부")
    private Boolean isPaid;

    @Schema(type = "integer", example = "2048", description = "네트워크 트래픽 사용량 (KB 단위)")
    private Long networkTraffic;

    @Schema(type = "string", example = "2023-01-01T12:00:00", description = "파일 저장 시간")
    private LocalDateTime storedAt;



    public static StorageLogResponseDto of(StorageLog storageLog) {
        return StorageLogResponseDto.builder()
                .id(storageLog.getId())
                .userId(storageLog.getUserId())
                .fileName(storageLog.getFileName())
                .objectKey(storageLog.getObjectKey())
                .isPaid(storageLog.getIsPaid())
                .networkTraffic(storageLog.getNetworkTraffic())
                .storedAt(storageLog.getStoredAt())
                .build();
    }
}
