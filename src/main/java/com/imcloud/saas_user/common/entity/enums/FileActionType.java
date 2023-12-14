package com.imcloud.saas_user.common.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileActionType {
    UPLOADED("UPLOADED"),          // 파일 업로드
    DOWNLOADED("DOWNLOADED"),      // 파일 다운로드
    DELETED("DELETED"),            // 파일 삭제
    TOBE_DEIDENTIFICATION("TOBE_DEIDENTIFICATION"); // 비식별처리 완료

    @JsonValue
    private final String value;
}
