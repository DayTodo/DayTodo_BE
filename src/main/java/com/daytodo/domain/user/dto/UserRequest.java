package com.daytodo.domain.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.daytodo.domain.user.enums.DevicePlatform;

import java.util.List;

public final class UserRequest {
    private UserRequest() {
    }

    public record ReplaceInterestRegions(
            @NotEmpty(message = "관심지역을 한 개 이상 선택해야 합니다.")
            List<@NotNull(message = "지역 ID는 null일 수 없습니다.") Long> regionIds
    ) {
    }

    public record UpdateNotificationSettings(
            @NotNull(message = "앱 푸시 알림 설정은 필수입니다.")
            Boolean pushEnabled
    ) {
    }

    public record RegisterFcmToken(
            @NotBlank(message = "FCM 토큰은 필수입니다.")
            @Size(max = 512, message = "FCM 토큰은 512자 이하여야 합니다.")
            String token,
            @NotNull(message = "기기 플랫폼은 필수입니다.")
            DevicePlatform platform
    ) {
    }

    public record DeleteFcmToken(
            @NotBlank(message = "FCM 토큰은 필수입니다.")
            @Size(max = 512, message = "FCM 토큰은 512자 이하여야 합니다.")
            String token
    ) {
    }

    public record SubmitFeedback(
            @NotNull(message = "의견 내용은 필수입니다.")
            String content
    ) {
    }

    public record ChangePassword(
            @NotBlank(message = "현재 비밀번호는 필수입니다.")
            String currentPassword,

            @NotBlank(message = "새 비밀번호는 필수입니다.")
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+]{8,}$",
                    message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다."
            )
            String newPassword
    ) {
    }
}