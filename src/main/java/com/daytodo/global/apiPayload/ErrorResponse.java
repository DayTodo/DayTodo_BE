package com.daytodo.global.apiPayload;

import com.daytodo.global.apiPayload.code.BaseErrorCode;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 에러(4xx, 5xx) 공통 응답 형식.
 * <pre>
 * {
 *   "status": 400,
 *   "code": "INVALID_DATE",
 *   "message": "조회 날짜가 올바르지 않습니다."
 * }
 * </pre>
 * (성공 응답은 래퍼 없이 데이터를 그대로 반환한다.)
 */
@Getter
@Builder
@AllArgsConstructor
@JsonPropertyOrder({"status", "code", "message"})
public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;

    public static ErrorResponse of(BaseErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    public static ErrorResponse of(BaseErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(message)
                .build();
    }
}
