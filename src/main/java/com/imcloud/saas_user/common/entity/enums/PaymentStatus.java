package com.imcloud.saas_user.common.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    COMPLETED("COMPLETED"),   // 결제 완료
    UNPAID("UNPAID"),         // 미납
    CANCELLED("CANCELLED");   // 결제 취소

    @JsonValue
    private final String value;
}