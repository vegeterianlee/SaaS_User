package com.imcloud.saas_user.common.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileActionType {
    DOWNLOADED("DOWNLOADED"),      // 파일 다운로드
    DELETED("DELETED"),            // 파일 삭제
    PENDING_DEIDENTIFICATION("PENDING_DEIDENTIFICATION"); // 비식별처리 대상 여부

    @JsonValue
    private final String value;
}
