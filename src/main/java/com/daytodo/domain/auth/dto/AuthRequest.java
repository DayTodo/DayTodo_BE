package com.daytodo.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthRequest {

    private AuthRequest() {
    }

    public record SignUp(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다.")
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+]{8,}$",
                    message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다."
            )
            String password,

            @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
            String nickname
    ) {
    }

    public record Login(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다.")
            String password
    ) {
    }

    public record NaverLogin(
            @NotBlank(message = "네이버 액세스 토큰은 필수입니다.")
            String naverAccessToken
    ) {
    }

    public record Reissue(
            @NotBlank(message = "리프레시 토큰은 필수입니다.")
            String refreshToken
    ) {
    }

    public record Logout(
            @NotBlank(message = "리프레시 토큰은 필수입니다.")
            String refreshToken
    ) {
    }

    public record SocialLink(
            @NotBlank(message = "provider는 필수입니다.")
            String provider,

            @NotBlank(message = "provider 토큰은 필수입니다.")
            String providerToken
    ) {
    }

    public record ResendVerificationEmail(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email
    ) {
    }

    public record ResetPasswordRequest(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email
    ) {
    }

    public record ResetPassword(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @NotBlank(message = "인증 코드는 필수입니다.")
            String code,

            @NotBlank(message = "새 비밀번호는 필수입니다.")
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+]{8,}$",
                    message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다."
            )
            String newPassword
    ) {
    }
}