package com.imcloud.saas_user.fileAction.dto;

import com.imcloud.saas_user.common.entity.FileAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FileActionDto {
    @Schema(type = "integer", example = "1", description = "로그 ID")
    private Long id;

    @Schema(type = "string", example = "user123", description = "사용자 ID")
    private String userId;

    @Schema(type = "string", example = "document.pdf", description = "파일 이름")
    private String fileName;

    @Schema(type = "string", example = "de-identification/folder/user123/document.pdf", description = "S3 버킷 내 객체 키")
    private String objectKey;

    @Schema(type = "boolean", example = "true", description = "비식별 처리된 여부")
    private Boolean toBeDeidentified;

    @Schema(type = "boolean", example = "true", description = "비식별 처리 대상 여부")
    private Boolean isDeidentifiedTarget;

    @Schema(type = "string", example = "2023-01-01T12:00:00", description = "파일 저장 시간")
    private LocalDateTime storedAt;

    @Schema(type = "string", example = "2023-01-02T12:00:00", description = "비식별 처리 시간")
    private LocalDateTime isDeidentifiedAt;


    public static FileActionDto of(FileAction fileAction) {
        return FileActionDto.builder()
                .id(fileAction.getId())
                .userId(fileAction.getUserId())
                .fileName(fileAction.getFileName())
                .objectKey(fileAction.getObjectKey())
                .toBeDeidentified(fileAction.getToBeDeidentified())
                .isDeidentifiedTarget(fileAction.getIsDeidentifiedTarget())
                .storedAt(fileAction.getStoredAt())
                .isDeidentifiedAt(fileAction.getIsDeidentifiedAt())
                .build();
    }
}
