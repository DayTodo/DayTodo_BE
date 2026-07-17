package com.daytodo.domain.auth.enums;

public enum UserStatus {
    PENDING,     // 이메일 인증 대기 (LOCAL 가입 시에만 거침)
    ACTIVE,      // 정상 활동 가능
    WITHDRAWN    // 탈퇴 처리됨
}
