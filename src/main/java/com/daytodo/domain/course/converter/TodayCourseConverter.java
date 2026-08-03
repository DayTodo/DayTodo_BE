package com.daytodo.domain.course.converter;

import com.daytodo.domain.course.dto.response.TodayCourseResponse;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.entity.CoursePlace;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.user.entity.User;

import java.util.List;

/**
 * Course -> 투데이 코스 조회/종료 응답 변환.
 */
public class TodayCourseConverter {

    private TodayCourseConverter() {
    }

    public static TodayCourseResponse.GetTodayCourse toTodayCourse(
            Course course,
            List<CourseMember> members,
            List<CoursePlace> coursePlaces
    ) {
        if (course == null) {
            return new TodayCourseResponse.GetTodayCourse(null);
        }

        return new TodayCourseResponse.GetTodayCourse(
                TodayCourseResponse.GetTodayCourse.TodayCourse.builder()
                        .courseId(course.getCourseId())
                        .courseName(course.getCourseName())
                        .members(members.stream().map(TodayCourseConverter::toMemberItem).toList())
                        .places(coursePlaces.stream().map(TodayCourseConverter::toPlaceItem).toList())
                        .build()
        );
    }

    public static TodayCourseResponse.CompleteCourse toCompleteCourse(Course course) {
        return TodayCourseResponse.CompleteCourse.builder()
                .courseId(course.getCourseId())
                .courseStatus(course.getCourseStatus())
                .completedAt(course.getCompletedAt())
                .build();
    }

    private static TodayCourseResponse.GetTodayCourse.MemberItem toMemberItem(CourseMember courseMember) {
        User user = courseMember.getUser();

        return TodayCourseResponse.GetTodayCourse.MemberItem.builder()
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    private static TodayCourseResponse.GetTodayCourse.PlaceItem toPlaceItem(CoursePlace coursePlace) {
        Place place = coursePlace.getPlace();

        return TodayCourseResponse.GetTodayCourse.PlaceItem.builder()
                .coursePlaceId(coursePlace.getCoursePlaceId())
                .placeOrder(coursePlace.getPlaceOrder())
                .placeName(place.getPlaceName())
                .category(place.getCategory())
                .build();
    }
}
