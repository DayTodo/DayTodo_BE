package com.daytodo.domain.course.controller;

import com.daytodo.domain.course.dto.CourseRequest;
import com.daytodo.domain.course.dto.CourseResponse;
import com.daytodo.domain.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Course")
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    private static final String TEMPORARY_USER_ID_HEADER = "X-User-Id";

    private final CourseService courseService;

    @Operation(summary = "홈 및 생성한 코스 목록 통합 조회")
    @GetMapping
    public CourseResponse.Courses getCourses(
            // TODO Auth 구현 후 인증 Principal에서 userId를 주입하도록 교체한다.
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return courseService.getCourses(userId, startDate, endDate);
    }

    @Operation(summary = "월별 코스 캘린더 조회")
    @GetMapping("/calendar")
    public CourseResponse.Calendar getCalendar(
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return courseService.getCalendar(userId, year, month);
    }

    @Operation(summary = "코스 생성")
    @PostMapping
    public CourseResponse.Created createCourse(
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId,
            @Valid @RequestBody CourseRequest.Create request
    ) {
        return courseService.createCourse(userId, request);
    }

    @Operation(summary = "초대코드로 코스 참가")
    @PostMapping("/join")
    public CourseResponse.Joined joinCourse(
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId,
            @Valid @RequestBody CourseRequest.Join request
    ) {
        return courseService.joinCourse(userId, request);
    }
}
