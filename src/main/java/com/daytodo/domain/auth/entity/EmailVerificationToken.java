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
 * ERD에는 없는 신규 테이블입니다. (이메일 인증 토큰 저장용)
 * 팀 ERD 반영 필요 여부 확인해주세요.
 */
@Getter
@Entity
@Table(name = "email_verification_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationToken {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "token", nullable = false, length = 100)
    private String token;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    public EmailVerificationToken(Long userId, String token, LocalDateTime expiredAt) {
        this.userId = userId;
        this.token = token;
        this.expiredAt = expiredAt;
    }

    public void update(String token, LocalDateTime expiredAt) {
        this.token = token;
        this.expiredAt = expiredAt;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiredAt.isBefore(now);
    }
}