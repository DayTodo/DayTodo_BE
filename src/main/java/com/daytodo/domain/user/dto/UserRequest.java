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
}
