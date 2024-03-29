package com.imcloud.saas_user.common.dto;

public enum ErrorType {
    NONE(""),
    EXCEPTION("Exception error"),
    RUNTIME_EXCEPTION("Runtime Exception error"),
    ENTITY_NOT_FOUND_EXCEPTION("entity not found error"),
    VALIDATION_EXCEPTION("validation fail error"),
    JWT_EXCEPTION("token invalid error"),
    BAD_CREDENTIALS_EXCEPTION("user's password is wrong"),
    ILLEGAL_ARGUMENT_EXCEPTION("wrong argument error"),
    ACCESS_DENIED_EXCEPTION("don't have authority to do so"),
    AUTHENTICATION_EXCEPTION("authentication has failed"),
    SQL_EXCEPTION("SQL ERROR"),
    IO_EXCEPTION("IOException error"),
    URI_SYNTAX_EXCEPTION("URI_SYNTAX error");

    String description;

    ErrorType(String description) {
        this.description = description;
    }

    public String getMessage() {
        return this.description;
    }
}
