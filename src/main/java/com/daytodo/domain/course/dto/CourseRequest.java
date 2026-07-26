package com.daytodo.domain.course.dto;

import com.daytodo.domain.course.enums.ParticipantType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class CourseRequest {
    private CourseRequest() {
    }

    public record Create(
            @NotBlank(message = "코스명은 필수입니다.")
            @Size(max = 20, message = "코스명은 20자 이하여야 합니다.")
            String courseName,
            @NotNull(message = "지역은 필수입니다.")
            Long regionId,
            @NotNull(message = "코스 날짜는 필수입니다.")
            LocalDate courseDate,
            @NotNull(message = "최소 금액은 필수입니다.")
            @PositiveOrZero(message = "최소 금액은 0 이상이어야 합니다.")
            Integer minPrice,
            @NotNull(message = "최대 금액은 필수입니다.")
            @PositiveOrZero(message = "최대 금액은 0 이상이어야 합니다.")
            Integer maxPrice,
            @NotNull(message = "동행 유형은 필수입니다.")
            ParticipantType participantType
    ) {
        @AssertTrue(message = "최대 금액은 최소 금액보다 작을 수 없습니다.")
        public boolean isValidPriceRange() {
            return minPrice == null || maxPrice == null || maxPrice >= minPrice;
        }
    }

    public record Join(
            @NotBlank(message = "초대코드는 필수입니다.")
            String inviteCode
    ) {
    }

    // ==============================================================================

    public record Setting(
            @NotBlank(message = "코스명은 필수입니다.")
            @Size(max = 20, message = "코스명은 20자 이하여야 합니다.")
            String courseName,
            @NotNull(message = "지역은 필수입니다.")
            Long regionId,
            @NotNull(message = "코스 날짜는 필수입니다.")
            LocalDate courseDate,
            @NotNull(message = "최소 금액은 필수입니다.")
            @PositiveOrZero(message = "최소 금액은 0 이상이어야 합니다.")
            Integer minPrice,
            @NotNull(message = "최대 금액은 필수입니다.")
            @PositiveOrZero(message = "최대 금액은 0 이상이어야 합니다.")
            Integer maxPrice,
            @NotNull(message = "동행 유형은 필수입니다.")
            ParticipantType participantType
    ) {
        @AssertTrue(message = "최대 금액은 최소 금액보다 작을 수 없습니다.")
        public boolean isValidPriceRange() {
            return minPrice == null || maxPrice == null || maxPrice >= minPrice;
        }
    }
}
