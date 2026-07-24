package com.daytodo.domain.auth.service;

import com.daytodo.domain.auth.client.NaverApiClient;
import com.daytodo.domain.auth.dto.AuthRequest;
import com.daytodo.domain.auth.dto.AuthResponse;
import com.daytodo.domain.auth.entity.EmailVerificationToken;
import com.daytodo.domain.auth.entity.PasswordResetToken;
import com.daytodo.domain.auth.entity.RefreshToken;
import com.daytodo.domain.auth.entity.SocialAccount;
import com.daytodo.domain.auth.enums.SocialProvider;
import com.daytodo.domain.auth.exception.code.AuthErrorCode;
import com.daytodo.domain.auth.repository.EmailVerificationTokenRepository;
import com.daytodo.domain.auth.repository.PasswordResetTokenRepository;
import com.daytodo.domain.auth.repository.RefreshTokenRepository;
import com.daytodo.domain.auth.repository.SocialAccountRepository;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import com.daytodo.global.security.jwt.JwtProperties;
import com.daytodo.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // TODO(팀 확인 필요): 5회 연속 실패 시 15분 잠금 정책(정책서 1장)을 반영하려면
    // Users 테이블에 실패 횟수/잠금 해제 시각 컬럼 추가가 필요합니다. ERD에는 아직 없습니다.

    private static final long EMAIL_VERIFICATION_EXPIRY_HOURS = 24;
    private static final long PASSWORD_RESET_EXPIRY_MINUTES = 10;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final NaverApiClient naverApiClient;
    private final AuthMailService authMailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public AuthResponse.EmailAvailability checkEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        return new AuthResponse.EmailAvailability(email, !exists);
    }

    @Transactional
    public AuthResponse.SignUp signUp(AuthRequest.SignUp request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ProjectException(AuthErrorCode.EMAIL_DUPLICATED);
        }
        String nickname = (request.nickname() == null || request.nickname().isBlank())
                ? generateDefaultNickname()
                : request.nickname();
        User user = userRepository.save(new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                nickname,
                null,
                LoginType.LOCAL
        ));
        // 이메일 인증 전까지는 INACTIVE 상태로 두고, 인증 완료 시 ACTIVE로 전환한다.
        user.changeStatus(UserStatus.INACTIVE);
        issueEmailVerificationToken(user);
        return new AuthResponse.SignUp(user.getId(), user.getEmail(), user.getNickname(), user.getCreatedAt());
    }

    @Transactional
    public AuthResponse.Login login(AuthRequest.Login request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ProjectException(AuthErrorCode.INVALID_CREDENTIALS));
        if (user.getUserStatus() == UserStatus.WITHDRAWN) {
            throw new ProjectException(AuthErrorCode.WITHDRAWN_USER);
        }
        if (user.getUserStatus() == UserStatus.INACTIVE) {
            throw new ProjectException(AuthErrorCode.EMAIL_NOT_VERIFIED);
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            // TODO 실패 횟수 증가 + 잠금 로직 연결 (위 TODO 참고)
            throw new ProjectException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        return issueLoginResponse(user);
    }

    @Transactional
    public AuthResponse.TokenPair reissue(AuthRequest.Reissue request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken())
                || !jwtTokenProvider.isRefreshToken(request.refreshToken())) {
            throw new ProjectException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        Long userId = jwtTokenProvider.getUserId(request.refreshToken());
        RefreshToken saved = refreshTokenRepository.findByUserIdAndToken(userId, request.refreshToken())
                .orElseThrow(() -> new ProjectException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
        saved.update(newRefreshToken, refreshExpiredAt());
        return new AuthResponse.TokenPair(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(AuthRequest.Logout request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            return;
        }
        Long userId = jwtTokenProvider.getUserId(request.refreshToken());
        refreshTokenRepository.findByUserIdAndToken(userId, request.refreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public AuthResponse.NaverLogin naverLogin(AuthRequest.NaverLogin request) {
        NaverApiClient.NaverProfileResponse profile = fetchNaverProfile(request.naverAccessToken());
        String providerUserId = profile.response().id();

        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(SocialProvider.NAVER, providerUserId)
                .orElse(null);

        boolean isNewUser = socialAccount == null;
        User user = (socialAccount != null)
                ? socialAccount.getUser()
                : createUserFromNaverProfile(profile);

        if (user.getUserStatus() == UserStatus.WITHDRAWN) {
            throw new ProjectException(AuthErrorCode.WITHDRAWN_USER);
        }

        AuthResponse.Login tokens = issueLoginResponse(user);
        return new AuthResponse.NaverLogin(tokens.accessToken(), tokens.refreshToken(), isNewUser, tokens.user());
    }

    @Transactional
    public AuthResponse.SocialLink linkNaverAccount(Long userId, AuthRequest.SocialLink request) {
        SocialProvider provider = SocialProvider.valueOf(request.provider().toUpperCase());
        User user = userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ProjectException(AuthErrorCode.WITHDRAWN_USER));

        // 1인 1 소셜계정 정책 (서비스 레이어에서 강제)
        if (socialAccountRepository.existsByUserIdAndProvider(userId, provider)) {
            throw new ProjectException(AuthErrorCode.ALREADY_LINKED_SAME_PROVIDER);
        }

        NaverApiClient.NaverProfileResponse profile = fetchNaverProfile(request.providerToken());
        String providerUserId = profile.response().id();

        if (socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId).isPresent()) {
            throw new ProjectException(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_LINKED);
        }

        SocialAccount socialAccount = socialAccountRepository.save(
                new SocialAccount(user, provider, providerUserId)
        );
        return new AuthResponse.SocialLink(socialAccount.getId(), provider.name(), socialAccount.getCreatedAt());
    }

    @Transactional
    public void resendVerificationEmail(AuthRequest.ResendVerificationEmail request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ProjectException(AuthErrorCode.EMAIL_NOT_FOUND));
        if (user.getUserStatus() != UserStatus.INACTIVE) {
            throw new ProjectException(AuthErrorCode.ALREADY_VERIFIED);
        }
        issueEmailVerificationToken(user);
    }

    @Transactional
    public AuthResponse.EmailVerified verifyEmail(String token) {
        EmailVerificationToken saved = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ProjectException(AuthErrorCode.INVALID_VERIFICATION_TOKEN));
        if (saved.isExpired(LocalDateTime.now(clock))) {
            throw new ProjectException(AuthErrorCode.EXPIRED_VERIFICATION_TOKEN);
        }
        User user = userRepository.findById(saved.getUserId())
                .orElseThrow(() -> new ProjectException(AuthErrorCode.EMAIL_NOT_FOUND));
        user.changeStatus(UserStatus.ACTIVE);
        emailVerificationTokenRepository.delete(saved);
        return new AuthResponse.EmailVerified(user.getEmail(), true);
    }

    @Transactional
    public void requestPasswordReset(AuthRequest.ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ProjectException(AuthErrorCode.EMAIL_NOT_FOUND));
        String code = generateResetCode();
        LocalDateTime expiredAt = LocalDateTime.now(clock).plusMinutes(PASSWORD_RESET_EXPIRY_MINUTES);
        passwordResetTokenRepository.findById(user.getId())
                .ifPresentOrElse(
                        existing -> existing.update(code, expiredAt),
                        () -> passwordResetTokenRepository.save(new PasswordResetToken(user.getId(), code, expiredAt))
                );
        authMailService.sendPasswordResetEmail(user.getEmail(), code);
    }

    @Transactional
    public void resetPassword(AuthRequest.ResetPassword request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ProjectException(AuthErrorCode.EMAIL_NOT_FOUND));
        PasswordResetToken saved = passwordResetTokenRepository.findById(user.getId())
                .orElseThrow(() -> new ProjectException(AuthErrorCode.INVALID_RESET_CODE));
        if (saved.isExpired(LocalDateTime.now(clock))) {
            throw new ProjectException(AuthErrorCode.EXPIRED_RESET_CODE);
        }
        if (!saved.getCode().equals(request.code())) {
            throw new ProjectException(AuthErrorCode.INVALID_RESET_CODE);
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        passwordResetTokenRepository.delete(saved);
    }

    private void issueEmailVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now(clock).plusHours(EMAIL_VERIFICATION_EXPIRY_HOURS);
        emailVerificationTokenRepository.findById(user.getId())
                .ifPresentOrElse(
                        existing -> existing.update(token, expiredAt),
                        () -> emailVerificationTokenRepository.save(
                                new EmailVerificationToken(user.getId(), token, expiredAt)
                        )
                );
        authMailService.sendVerificationEmail(user.getEmail(), token);
    }

    private String generateResetCode() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }

    private NaverApiClient.NaverProfileResponse fetchNaverProfile(String naverAccessToken) {
        NaverApiClient.NaverProfileResponse profile = naverApiClient.getProfile(naverAccessToken);
        if (profile == null || !"00".equals(profile.resultcode())) {
            throw new ProjectException(AuthErrorCode.NAVER_API_ERROR);
        }
        return profile;
    }

    // TODO(팀 확인 필요): 네이버 동의항목에서 이메일 제공에 동의하지 않으면 email이 null일 수 있습니다.
    // User.email이 NOT NULL + UNIQUE라 이 경우 예외 처리가 필요합니다.
    private User createUserFromNaverProfile(NaverApiClient.NaverProfileResponse profile) {
        String nickname = (profile.response().nickname() == null || profile.response().nickname().isBlank())
                ? generateDefaultNickname()
                : profile.response().nickname();
        User user = userRepository.save(new User(
                profile.response().email(),
                null,
                nickname,
                null,
                LoginType.NAVER
        ));
        socialAccountRepository.save(new SocialAccount(user, SocialProvider.NAVER, profile.response().id()));
        return user;
    }

    private AuthResponse.Login issueLoginResponse(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        saveOrUpdateRefreshToken(user.getId(), refreshToken);
        return new AuthResponse.Login(
                accessToken,
                refreshToken,
                new AuthResponse.UserSummary(user.getId(), user.getNickname())
        );
    }

    private void saveOrUpdateRefreshToken(Long userId, String token) {
        refreshTokenRepository.findById(userId)
                .ifPresentOrElse(
                        existing -> existing.update(token, refreshExpiredAt()),
                        () -> refreshTokenRepository.save(new RefreshToken(userId, token, refreshExpiredAt()))
                );
    }

    private LocalDateTime refreshExpiredAt() {
        return LocalDateTime.ofInstant(
                Instant.now(clock).plusMillis(jwtProperties.expiration().refresh()),
                ZoneId.systemDefault()
        );
    }

    // TODO(팀 확인 필요): 단순 count+1 방식이라 동시 가입 시 닉네임이 겹칠 수 있습니다.
    // 시퀀스/UUID 기반으로 교체하는 게 안전합니다.
    private String generateDefaultNickname() {
        long count = userRepository.count() + 1;
        return String.format("투두%02d", count);
    }
}