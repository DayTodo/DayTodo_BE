package com.daytodo.domain.course.dto.response;

import com.daytodo.domain.course.enums.CourseStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class CourseResDTO {

    // 투데이 코스 조회 (오늘 진행 중인 코스가 없으면 todayCourse 는 null)
    public record GetTodayCourse(
            TodayCourse todayCourse
    ){
        @Builder
        public record TodayCourse(
                Long courseId,
                String courseName,
                List<MemberItem> members,
                List<PlaceItem> places
        ){}

        @Builder
        public record MemberItem(
                String nickname,
                String profileImageUrl
        ){}

        @Builder
        public record PlaceItem(
                Long coursePlaceId,
                Integer placeOrder,
                String placeName,
                String category
        ){}
    }

    // 코스 종료
    @Builder
    public record CompleteCourse(
            Long courseId,
            CourseStatus courseStatus,
            LocalDateTime completedAt
    ){}
}
