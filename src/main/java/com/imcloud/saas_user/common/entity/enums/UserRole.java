package com.imcloud.saas_user.common.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("USER"),
    ADMIN("ADMIN");

    @JsonValue
    private final String value;

    public static UserRole fromString(String roleStr) {
        for (UserRole role : UserRole.values()) {
            if (role.getValue().equalsIgnoreCase(roleStr)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No enum constant for role string: " + roleStr);
    }
}
