package com.daytodo.domain.course.dto;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.enums.ParticipantType;

import java.time.LocalDate;

public class CourseResDto {

    // [코스 구성] 코스 편집
    public record SettingRes(
            Long courseId,
            String courseName,
            Long regionId,
            LocalDate courseDate,
            Integer minPrice,
            Integer maxPrice,
            ParticipantType participantType
    ) {
        public static SettingRes from(Course course) {
            return new SettingRes(
                    course.getCourseId(),
                    course.getCourseName(),
                    course.getRegionId(),
                    course.getCourseDate(),
                    course.getMinPrice(),
                    course.getMaxPrice(),
                    course.getParticipantType()
            );
        }
    }

}