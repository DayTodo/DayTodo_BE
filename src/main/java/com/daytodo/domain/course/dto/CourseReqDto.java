package com.daytodo.domain.course.dto;

import com.daytodo.domain.course.enums.ParticipantType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CourseReqDto {

    // [코스 구성] 코스 편집
    public record SettingReq(
            @NotBlank(message = "코스명은 필수입니다.")
            @Size(max = 20, message = "코스명은 최대 20자입니다.")
            String courseName,

            @NotNull(message = "지역 ID는 필수입니다.")
            Long regionId,

            @NotNull(message = "코스 날짜는 필수입니다.")
            LocalDate courseDate,

            @NotNull(message = "최소 가격은 필수입니다.")
            @PositiveOrZero(message = "최소 가격은 0 이상이어야 합니다.")
            Integer minPrice,

            @NotNull(message = "최대 가격은 필수입니다.")
            @PositiveOrZero(message = "최대 가격은 0 이상이어야 합니다.")
            Integer maxPrice,

            @NotNull(message = "인원 유형은 필수입니다.")
            ParticipantType participantType
    ) {}

}