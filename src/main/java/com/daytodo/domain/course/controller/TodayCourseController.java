package com.daytodo.domain.course.controller;

import com.daytodo.domain.course.dto.request.TodayCourseRequest;
import com.daytodo.domain.course.dto.response.TodayCourseResponse;
import com.daytodo.domain.course.service.TodayCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
public class TodayCourseController {

    private final TodayCourseService todayCourseService;

    @GetMapping("/today")
    public TodayCourseResponse.GetTodayCourse getTodayCourse(
            @AuthenticationPrincipal Long userId
    ) {
        return todayCourseService.getTodayCourse(userId);
    }

    @PostMapping("/{courseId}/complete")
    public TodayCourseResponse.CompleteCourse completeCourse(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId
    ) {
        return todayCourseService.completeCourse(userId, courseId);
    }

    @PostMapping("/{courseId}/photos")
    public TodayCourseResponse.SaveMemoryPhotos saveMemoryPhotos(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId,
            @RequestBody TodayCourseRequest.SaveMemoryPhotos request
    ) {
        return todayCourseService.saveMemoryPhotos(userId, courseId, request);
    }
}
