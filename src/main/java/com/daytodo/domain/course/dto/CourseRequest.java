package com.daytodo.domain.course.dto;

import com.daytodo.domain.course.enums.ParticipantType;
import com.daytodo.domain.course.enums.RecommendationSource;
import com.daytodo.domain.place.enums.PlaceRecommendationSource;
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

    public record AiRecommendation(
            @NotNull(message = "지역은 필수입니다.") Long regionId,
            @NotNull(message = "최소 금액은 필수입니다.")
            @PositiveOrZero(message = "최소 금액은 0 이상이어야 합니다.") Integer minPrice,
            @NotNull(message = "최대 금액은 필수입니다.")
            @PositiveOrZero(message = "최대 금액은 0 이상이어야 합니다.") Integer maxPrice
    ) {
        @AssertTrue(message = "최대 금액은 최소 금액보다 작을 수 없습니다.")
        public boolean isValidPriceRange() {
            return minPrice == null || maxPrice == null || maxPrice >= minPrice;
        }
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

    public record AddCoursePlace(
            @NotNull(message = "recommendationId는 필수입니다.")
            Long recommendationId
    ) {
    }

    public record RecommendPlace(
            @NotNull(message = "추천 출처는 필수입니다.")
            PlaceRecommendationSource source, // [변경 완료] 기존 PlaceRecommendationSource 사용
            @NotNull(message = "placeId는 필수입니다.")
            Long placeId
    ) {
    }

    public record RecommendationComment(
            @NotBlank(message = "댓글 내용은 필수입니다.")
            @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
            String content
    ) {
    }
}
