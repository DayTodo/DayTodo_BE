package com.daytodo.domain.user.exception.code;

import com.daytodo.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_WITHDRAWN(HttpStatus.CONFLICT, "USER_ALREADY_WITHDRAWN", "이미 탈퇴한 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
