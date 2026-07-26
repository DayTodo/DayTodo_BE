package com.daytodo.domain.course.dto;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.entity.CoursePlace;
import com.daytodo.domain.course.enums.MemberRole;
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

                    course.getRegion().getRegionId(),

                    course.getCourseDate(),
                    course.getMinPrice(),
                    course.getMaxPrice(),
                    course.getParticipantType()
            );
        }
    }

    // [코스 구성] 코스 장소 목록 조회
    public record CoursePlaceRes(
            Long coursePlaceId,
            Long placeId,
            String placeName,
            Integer placeOrder
    ) {
        public static CoursePlaceRes from(CoursePlace coursePlace) {
            return new CoursePlaceRes(
                    coursePlace.getCoursePlaceId(),

                    // Place 엔티티의 placeId 필드를 가져옵니다.
                    coursePlace.getPlace().getPlaceId(),

                    // Place 엔티티의 placeName 필드를 가져옵니다. (TODO 해결완료!)
                    coursePlace.getPlace().getPlaceName(),

                    coursePlace.getPlaceOrder()
            );
        }
    }

    // [코스 구성] 코스 멤버 목록 조회
    public record CourseMemberRes(
            Long memberId,
            String nickname,
            MemberRole memberRole
    ) {
        public static CourseMemberRes from(CourseMember courseMember) {
            return new CourseMemberRes(
                    courseMember.getUser().getId(),
                    courseMember.getUser().getNickname(),
                    courseMember.getMemberRole()
            );
        }
    }
}
