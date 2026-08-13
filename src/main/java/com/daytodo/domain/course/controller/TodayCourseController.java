package com.daytodo.domain.course.controller;

import com.daytodo.domain.course.dto.request.TodayCourseRequest;
import com.daytodo.domain.course.dto.response.TodayCourseResponse;
import com.daytodo.domain.course.service.TodayCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Today Course")
@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
public class TodayCourseController {

    private final TodayCourseService todayCourseService;

    @Operation(summary = "투데이 코스 조회")
    @GetMapping("/today")
    public TodayCourseResponse.GetTodayCourse getTodayCourse(
            @AuthenticationPrincipal Long userId
    ) {
        return todayCourseService.getTodayCourse(userId);
    }

    @Operation(summary = "투데이 코스 종료")
    @PostMapping("/{courseId}/complete")
    public TodayCourseResponse.CompleteCourse completeCourse(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId
    ) {
        return todayCourseService.completeCourse(userId, courseId);
    }

    @Operation(
            summary = "투데이 추억 사진 저장",
            description = "이미지 파일(JPG/PNG, 각 5MB 이하)을 multipart/form-data 의 images 파트로 업로드한다. "
                    + "서버가 S3 에 저장하고 영구 URL 로 치환해 기록한다. (기기 로컬 URI 를 그대로 저장하지 않는다.)"
    )
    @PostMapping(value = "/{courseId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TodayCourseResponse.SaveMemoryPhotos saveMemoryPhotos(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return todayCourseService.saveMemoryPhotos(userId, courseId, images);
    }

    @Operation(summary = "투데이 장소 삭제")
    @DeleteMapping("/{courseId}/today-places/{coursePlaceId}")
    public TodayCourseResponse.GetCoursePlaces removePlaceFromCourse(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId,
            @PathVariable Long coursePlaceId
    ) {
        return todayCourseService.removePlaceFromCourse(userId, courseId, coursePlaceId);
    }

    @Operation(summary = "투데이 장소 순서 변경")
    @PatchMapping("/{courseId}/today-places/order")
    public TodayCourseResponse.GetCoursePlaces reorderCoursePlaces(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId,
            @RequestBody TodayCourseRequest.ReorderCoursePlaces request
    ) {
        return todayCourseService.reorderCoursePlaces(userId, courseId, request);
    }
}
