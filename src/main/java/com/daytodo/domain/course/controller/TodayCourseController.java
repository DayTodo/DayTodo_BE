package com.daytodo.domain.course.controller;

import com.daytodo.domain.course.dto.request.TodayCourseRequest;
import com.daytodo.domain.course.dto.response.TodayCourseResponse;
import com.daytodo.domain.course.service.TodayCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "투데이 추억 사진 저장")
    @PostMapping("/{courseId}/photos")
    public TodayCourseResponse.SaveMemoryPhotos saveMemoryPhotos(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId,
            @RequestBody TodayCourseRequest.SaveMemoryPhotos request
    ) {
        return todayCourseService.saveMemoryPhotos(userId, courseId, request);
    }

    @Operation(summary = "투데이 장소 추가")
    @PostMapping("/{courseId}/today-places")
    public TodayCourseResponse.GetCoursePlaces addPlaceToCourse(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId,
            @RequestBody TodayCourseRequest.AddPlace request
    ) {
        return todayCourseService.addPlaceToCourse(userId, courseId, request);
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
