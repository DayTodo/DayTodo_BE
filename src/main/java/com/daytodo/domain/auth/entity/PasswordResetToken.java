package com.daytodo.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ERD에는 없는 신규 테이블입니다. (비밀번호 재설정 인증 코드 저장용)
 * 정책서: 비밀번호 분실 시 이메일 인증으로 코드를 보내 재설정.
 */
@Getter
@Entity
@Table(name = "password_reset_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    public PasswordResetToken(Long userId, String code, LocalDateTime expiredAt) {
        this.userId = userId;
        this.code = code;
        this.expiredAt = expiredAt;
    }

    public void update(String code, LocalDateTime expiredAt) {
        this.code = code;
        this.expiredAt = expiredAt;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiredAt.isBefore(now);
    }
}