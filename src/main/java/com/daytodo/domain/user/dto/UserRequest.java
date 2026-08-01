package com.daytodo.domain.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
            Boolean pushEnabled,
            @NotNull(message = "D-1 코스 알림 설정은 필수입니다.")
            Boolean courseD1Enabled,
            @NotNull(message = "D-0 코스 알림 설정은 필수입니다.")
            Boolean courseD0Enabled
    ) {
    }

    public record SubmitFeedback(
            @NotNull(message = "의견 내용은 필수입니다.")
            String content
    ) {
    }
}
