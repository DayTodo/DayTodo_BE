package com.daytodo.domain.course.dto;

import com.daytodo.domain.course.enums.HomeBannerStatus;
import com.daytodo.domain.course.enums.ParticipantType;

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
}
