package com.daytodo.domain.auth.dto;

import java.time.LocalDateTime;

public final class AuthResponse {

    private AuthResponse() {
    }

    public record SignUp(Long userId, String email, String nickname, LocalDateTime createdAt) {
    }

    public record EmailAvailability(String email, boolean available) {
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }

    public record Login(String accessToken, String refreshToken, UserSummary user) {
    }

    public record NaverLogin(String accessToken, String refreshToken, boolean isNewUser, UserSummary user) {
    }

    public record UserSummary(Long userId, String nickname) {
    }

    public record SocialLink(Long socialAccountId, String provider, LocalDateTime linkedAt) {
    }

    public record EmailVerified(String email, boolean verified) {
    }
}