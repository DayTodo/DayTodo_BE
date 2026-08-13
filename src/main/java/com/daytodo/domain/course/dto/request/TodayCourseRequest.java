package com.daytodo.domain.course.dto.request;

import java.util.List;

public class TodayCourseRequest {

    // 추억 사진 저장은 multipart/form-data(파일 업로드)로 처리하므로 별도 요청 DTO 를 두지 않는다.

    // 코스 장소 순서 변경 : 배열 순서대로 placeOrder 갱신. 검증은 서비스에서
    public record ReorderCoursePlaces(
            List<Long> orderedCoursePlaceIds
    ){}
}
