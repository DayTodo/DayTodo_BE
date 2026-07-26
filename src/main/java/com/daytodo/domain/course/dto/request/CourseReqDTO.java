package com.daytodo.domain.course.dto.request;

import java.util.List;

public class CourseReqDTO {

    /*
     * 추억 사진 저장
     * 빈 목록/공백 URL 검증은 서비스에서 수행한다.
     * (명세가 요구하는 INVALID_PARAMETER 코드로 응답하기 위해 Bean Validation 을 쓰지 않음)
     */
    public record SaveMemoryPhotos(
            List<String> imageUrls
    ){}
}
