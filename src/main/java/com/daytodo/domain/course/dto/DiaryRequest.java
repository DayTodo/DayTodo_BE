package com.daytodo.domain.course.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class DiaryRequest {

    private DiaryRequest() {
    }

    public record Write(
            @NotNull(message = "코스 ID는 필수입니다.")
            Long courseId,

            @Size(max = 1000, message = "일기 내용은 1000자 이하여야 합니다.")
            String content
    ) {
    }
}