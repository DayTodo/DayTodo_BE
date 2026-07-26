package com.daytodo.domain.auth.controller;

import com.daytodo.domain.auth.dto.AuthRequest;
import com.daytodo.domain.auth.dto.AuthResponse;
import com.daytodo.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/email-check")
    public AuthResponse.EmailAvailability checkEmail(@RequestParam String email) {
        return authService.checkEmail(email);
    }

    @Operation(summary = "자체 회원가입")
    @PostMapping("/register")
    public AuthResponse.SignUp signUp(@Valid @RequestBody AuthRequest.SignUp request) {
        return authService.signUp(request);
    }

    @Operation(summary = "자체 로그인")
    @PostMapping("/login")
    public AuthResponse.Login login(@Valid @RequestBody AuthRequest.Login request) {
        return authService.login(request);
    }

    @Operation(summary = "네이버 로그인")
    @PostMapping("/login/naver")
    public AuthResponse.NaverLogin naverLogin(@Valid @RequestBody AuthRequest.NaverLogin request) {
        return authService.naverLogin(request);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody AuthRequest.Logout request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/token/refresh")
    public AuthResponse.TokenPair reissue(@Valid @RequestBody AuthRequest.Reissue request) {
        return authService.reissue(request);
    }

    @Operation(summary = "계정 연동 처리")
    @PostMapping("/link/naver")
    public AuthResponse.SocialLink linkNaverAccount(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AuthRequest.SocialLink request
    ) {
        return authService.linkNaverAccount(userId, request);
    }

    @Operation(summary = "인증 메일 재전송")
    @PostMapping("/verify-email/resend")
    public ResponseEntity<Void> resendVerificationEmail(
            @Valid @RequestBody AuthRequest.ResendVerificationEmail request
    ) {
        authService.resendVerificationEmail(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "이메일 인증 처리")
    @GetMapping("/verify-email")
    public AuthResponse.EmailVerified verifyEmail(@RequestParam String token) {
        return authService.verifyEmail(token);
    }

    @Operation(summary = "비밀번호 재설정 요청")
    @PostMapping("/password/reset-request")
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody AuthRequest.ResetPasswordRequest request
    ) {
        authService.requestPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "비밀번호 재설정")
    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody AuthRequest.ResetPassword request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}