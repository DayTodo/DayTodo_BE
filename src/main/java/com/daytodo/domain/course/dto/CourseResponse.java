package com.daytodo.domain.course.dto;

import com.daytodo.domain.course.enums.HomeBannerStatus;
import com.daytodo.domain.course.enums.ParticipantType;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.enums.MemberRole;
import com.daytodo.domain.course.enums.MemberStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class CourseResponse {
    private CourseResponse() {
    }

    public record Banner(HomeBannerStatus status, String message, Long courseId) {
    }

    public record CourseCard(
            Long courseId,
            String courseName,
            LocalDate courseDate,
            long dDay,
            long memberCount,
            ParticipantType participantType
    ) {
    }

    public record CreatedCourse(
            Long courseId,
            String courseName,
            LocalDate courseDate,
            long memberCount,
            long placeCount,
            ParticipantType participantType
    ) {
    }

    public record Courses(
            Banner banner,
            List<CourseCard> inProgressCourses,
            List<CourseCard> upcomingCourses,
            List<CreatedCourse> createdCourses
    ) {
        public Courses {
            inProgressCourses = List.copyOf(inProgressCourses);
            upcomingCourses = List.copyOf(upcomingCourses);
            createdCourses = List.copyOf(createdCourses);
        }
    }

    public record CalendarCourse(
            Long courseId,
            String courseName,
            ParticipantType participantType,
            long memberCount
    ) {
    }

    public record DateSchedule(LocalDate date, List<CalendarCourse> courses) {
        public DateSchedule {
            courses = List.copyOf(courses);
        }
    }

    public record Calendar(int year, int month, List<DateSchedule> schedules) {
        public Calendar {
            schedules = List.copyOf(schedules);
        }
    }

    public record Created(Long courseId, String inviteCode, LocalDateTime inviteCodeExpiredAt) {
    }

    public record Joined(Long courseId, String courseName) {
    }

// ==============================================================================

    public record Setting(
            Long courseId,
            String courseName,
            Long regionId,
            LocalDate courseDate,
            Integer minPrice,
            Integer maxPrice,
            ParticipantType participantType
    ) {
        public static Setting from(Course course) {
            return new Setting(
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

    public record CoursePlace(
            Long coursePlaceId,
            Long placeId,
            String placeName,
            Integer placeOrder
    ) {
        public static CoursePlace from(com.daytodo.domain.course.entity.CoursePlace coursePlace) {
            return new CoursePlace(
                    coursePlace.getCoursePlaceId(),
                    coursePlace.getPlace().getPlaceId(),
                    coursePlace.getPlace().getPlaceName(),
                    coursePlace.getPlaceOrder()
            );
        }
    }

    public record CourseMember(
            Long courseMemberId,
            Long userId,
            String nickname,
            MemberRole memberRole,
            MemberStatus memberStatus
    ) {
        public static CourseMember from(com.daytodo.domain.course.entity.CourseMember courseMember) {
            return new CourseMember(
                    courseMember.getCourseMemberId(),
                    courseMember.getUser().getId(),       // 수정 완료: User 엔티티의 실제 ID Getter
                    courseMember.getUser().getNickname(), // 수정 완료: User 엔티티의 실제 닉네임 Getter
                    courseMember.getMemberRole(),
                    courseMember.getMemberStatus()
            );
        }
    }
}
