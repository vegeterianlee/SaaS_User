package com.imcloud.saas_user.fileAction.dto;

import com.imcloud.saas_user.common.entity.FileAction;
import com.imcloud.saas_user.common.entity.FileActionHistory;
import com.imcloud.saas_user.common.entity.enums.FileActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FileActionHistoryDto {
    @Schema(type = "integer", example = "1", description = "히스토리 ID")
    private Long id;

    @Schema(type = "string", example = "document.pdf", description = "파일 이름")
    private String fileName;

    @Schema(type = "string", example = "de-identification/folder/user123/document.pdf", description = "S3 버킷 내 객체 키")
    private String objectKey;

    @Schema(type = "string", description = "파일 액션 타입")
    private FileActionType actionType;

    @Schema(type = "string", example = "2023-01-01T12:00:00", description = "액션 발생 시간")
    private LocalDateTime actionTime;

    // 여기서 fileActionId는 FileAction 엔티티의 ID를 참조합니다.
    @Schema(type = "integer", example = "1", description = "관련 파일 로그정보 ID")
    private Long fileId;

    public static FileActionHistoryDto of(FileActionHistory fileActionHistory) {
        return FileActionHistoryDto.builder()
                .id(fileActionHistory.getId())
                .fileName(fileActionHistory.getFileName())
                .objectKey(fileActionHistory.getObjectKey())
                .actionType(fileActionHistory.getActionType())
                .actionTime(fileActionHistory.getActionTime())
                .fileId(fileActionHistory.getFileActionId())
                .build();
    }
}