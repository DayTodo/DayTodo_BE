package com.daytodo.domain.course.controller;

import com.daytodo.domain.course.dto.CourseRequest;
import com.daytodo.domain.course.dto.CourseResponse;
import com.daytodo.domain.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @Operation(summary = "코스 편집")
    @PatchMapping("/{courseId}/setting")
    public CourseResponse.Setting updateCourseSetting(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CourseRequest.Setting request
    ) {
        return courseService.updateCourseSetting(courseId, userId, request);
    }

    @Operation(summary = "코스 장소 리스트 조회")
    @GetMapping("/{courseId}/places")
    public List<CourseResponse.CoursePlace> getCoursePlaces(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Long userId
    ) {
        return courseService.getCoursePlaces(courseId, userId);
    }

    @Operation(summary = "코스 멤버 목록 조회")
    @GetMapping("/{courseId}/members")
    public List<CourseResponse.CourseMember> getCourseMembers(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Long userId
    ) {
        return courseService.getCourseMembers(courseId, userId);
    }

    @Operation(summary = "코스 멤버 강퇴")
    @DeleteMapping("/{courseId}/members/{memberId}")
    public void kickCourseMember(
            @PathVariable Long courseId,
            // URL 호환성을 위해 memberId라는 경로명은 유지한다.
            // 이 값은 CourseMember PK가 아니라 강퇴 대상 사용자의 userId다.
            @PathVariable Long memberId,
            @AuthenticationPrincipal Long userId
    ) {
        courseService.kickCourseMember(courseId, memberId, userId);
    }

}
