package com.daytodo.domain.auth.exception.code;

import com.daytodo.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "EMAIL_DUPLICATED", "이미 가입된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 일치하지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.LOCKED, "ACCOUNT_LOCKED", "로그인 실패 횟수 초과로 계정이 잠겼습니다. 잠시 후 다시 시도해주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "리프레시 토큰이 유효하지 않습니다. 다시 로그인해주세요."),
    WITHDRAWN_USER(HttpStatus.FORBIDDEN, "WITHDRAWN_USER", "탈퇴한 계정입니다."),
    SOCIAL_ACCOUNT_ALREADY_LINKED(HttpStatus.CONFLICT, "SOCIAL_ACCOUNT_ALREADY_LINKED", "이미 다른 계정에 연동된 소셜 계정입니다."),
    ALREADY_LINKED_SAME_PROVIDER(HttpStatus.CONFLICT, "ALREADY_LINKED_SAME_PROVIDER", "이미 해당 provider로 연동된 계정이 있습니다."),
    NAVER_API_ERROR(HttpStatus.BAD_GATEWAY, "NAVER_API_ERROR", "네이버 인증 서버와 통신 중 오류가 발생했습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다."),
    ALREADY_VERIFIED(HttpStatus.CONFLICT, "ALREADY_VERIFIED", "이미 인증이 완료된 계정입니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL_NOT_FOUND", "가입되지 않은 이메일입니다."),
    INVALID_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_TOKEN", "유효하지 않은 인증 토큰입니다."),
    EXPIRED_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, "EXPIRED_VERIFICATION_TOKEN", "만료된 인증 토큰입니다. 인증 메일을 재전송해주세요."),
    INVALID_RESET_CODE(HttpStatus.BAD_REQUEST, "INVALID_RESET_CODE", "인증 코드가 일치하지 않습니다."),
    EXPIRED_RESET_CODE(HttpStatus.BAD_REQUEST, "EXPIRED_RESET_CODE", "인증 코드가 만료되었습니다. 재설정을 다시 요청해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}