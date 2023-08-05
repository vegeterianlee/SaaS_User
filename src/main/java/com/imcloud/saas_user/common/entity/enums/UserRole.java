package com.imcloud.saas_user.common.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    User("USER"),
    Admin("ADMIN");

    @JsonValue
    private final String value;
}
