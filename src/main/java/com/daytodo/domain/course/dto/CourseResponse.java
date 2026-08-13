package com.daytodo.domain.course.dto;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.HomeBannerStatus;
import com.daytodo.domain.course.enums.MemberRole;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.enums.ParticipantType;
import com.daytodo.domain.place.enums.PlaceRecommendationSource;

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
            long memberCount,
            CourseStatus courseStatus
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

    public record AiRecommendations(
            boolean success,
            String code,
            String message,
            List<AiRecommendationCourse> result
    ) {
        public AiRecommendations {
            result = List.copyOf(result);
        }
    }

    public record AiRecommendationCourse(
            String courseName,
            int estimatedTotalMinPrice,
            int estimatedTotalMaxPrice,
            List<AiRecommendationPlace> places
    ) {
        public AiRecommendationCourse {
            places = List.copyOf(places);
        }
    }

    public record AiRecommendationPlace(
            int recommendationOrder,
            Long placeId,
            String naverPlaceId,
            String placeName,
            String category,
            String address,
            String roadAddress,
            double latitude,
            double longitude,
            String description,
            String imageUrl,
            int minPrice,
            int maxPrice,
            double priceConfidence,
            String priceReason
    ) {
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

    // ==============================================================================

    public record RecommendationLike(
            Long recommendationLikeId,
            int likeCount,
            boolean isLiked
    ) {
    }

    public record CoursePlaceAdded(
            Long coursePlaceId
    ) {
    }

    public record RecommendationCreated(
            Long recommendationId
    ) {
    }

    public record RecommendationCommentCreated(
            Long commentId
    ) {
    }

    public record Recommendation(
            Long recommendationId,
            PlaceRecommendationSource source, // String -> PlaceRecommendationSource로 변경
            String placeName,
            int likeCount,
            int commentCount,
            boolean isLiked,
            boolean isSelected
    ) {}
}
