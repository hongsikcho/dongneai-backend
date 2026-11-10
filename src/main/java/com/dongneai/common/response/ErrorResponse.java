package com.dongneai.common.response;

import com.dongneai.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, Object> details
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, Map<String, Object> details) {
        return new ErrorResponse(errorCode.name(), message, details);
    }
}
