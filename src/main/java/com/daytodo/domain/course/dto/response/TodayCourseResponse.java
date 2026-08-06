package com.daytodo.domain.course.dto.response;

import com.daytodo.domain.course.enums.CourseStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class TodayCourseResponse {

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

    // 코스 장소 추가/순서변경 응답: 추가·변경 후 전체 목록을 순서대로 반환
    // (투데이 조회의 장소 목록과 동일한 형태 재사용: coursePlaceId/placeOrder/placeName/category)
    public record GetCoursePlaces(
            List<GetTodayCourse.PlaceItem> places
    ){}

    // 코스 종료
    @Builder
    public record CompleteCourse(
            Long courseId,
            CourseStatus courseStatus,
            LocalDateTime completedAt
    ){}

    // 추억 사진 저장
    public record SaveMemoryPhotos(
            Integer savedCount,
            List<PhotoItem> photos
    ){
        @Builder
        public record PhotoItem(
                Long memoryPhotoId,
                String imageUrl,
                Integer photoOrder
        ){}
    }
}
