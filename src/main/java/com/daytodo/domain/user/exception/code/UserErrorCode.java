package com.daytodo.domain.user.exception.code;

import com.daytodo.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_WITHDRAWN(HttpStatus.CONFLICT, "USER_ALREADY_WITHDRAWN", "이미 탈퇴한 사용자입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "INVALID_NICKNAME", "닉네임은 공백이 아닌 20자 이하여야 합니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "NICKNAME_DUPLICATED", "이미 사용 중인 닉네임입니다."),
    INVALID_PROFILE_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_PROFILE_IMAGE_FORMAT", "JPG 또는 PNG 이미지만 업로드할 수 있습니다."),
    PROFILE_IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "PROFILE_IMAGE_TOO_LARGE", "프로필 이미지는 5MB 이하여야 합니다."),
    PROFILE_IMAGE_STORAGE_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "PROFILE_IMAGE_STORAGE_NOT_CONFIGURED", "프로필 이미지 저장소 설정이 완료되지 않았습니다."),
    PROFILE_IMAGE_UPLOAD_FAILED(HttpStatus.BAD_GATEWAY, "PROFILE_IMAGE_UPLOAD_FAILED", "프로필 이미지를 저장하지 못했습니다."),
    FEEDBACK_TOO_SHORT(HttpStatus.BAD_REQUEST, "FEEDBACK_TOO_SHORT", "의견은 공백을 제거한 후 100자 이상이어야 합니다."),
    POLICY_NOT_AVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "POLICY_NOT_AVAILABLE", "약관 및 정책을 불러오지 못했습니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_CURRENT_PASSWORD", "현재 비밀번호가 일치하지 않습니다."),
    SOCIAL_ACCOUNT_PASSWORD_CHANGE_NOT_ALLOWED(HttpStatus.CONFLICT, "SOCIAL_ACCOUNT_PASSWORD_CHANGE_NOT_ALLOWED", "소셜 로그인 계정은 비밀번호 변경을 지원하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}