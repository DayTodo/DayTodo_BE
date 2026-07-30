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
    private final CourseService courseService;

    @Operation(summary = "홈 및 생성한 코스 목록 통합 조회")
    @GetMapping
    public CourseResponse.Courses getCourses(
            @AuthenticationPrincipal Long userId,
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
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return courseService.getCalendar(userId, year, month);
    }

    @Operation(summary = "코스 생성")
    @PostMapping
    public CourseResponse.Created createCourse(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CourseRequest.Create request
    ) {
        return courseService.createCourse(userId, request);
    }

    @Operation(summary = "초대코드로 코스 참가")
    @PostMapping("/join")
    public CourseResponse.Joined joinCourse(
            @AuthenticationPrincipal Long userId,
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

    @Operation(summary = "추천 장소 좋아요")
    @PostMapping("/recommendations/{recommendationId}/likes")
    public CourseResponse.RecommendationLike likeRecommendation(
            @PathVariable Long recommendationId,
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId // 수정됨
    ) {
        return courseService.likeRecommendation(recommendationId, userId);
    }

    @Operation(summary = "추천 장소 코스에 넣기")
    @PostMapping("/{courseId}/places")
    public CourseResponse.CoursePlaceAdded addRecommendationToCourse(
            @PathVariable Long courseId,
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId, // 수정됨
            @Valid @RequestBody CourseRequest.AddCoursePlace request
    ) {
        return courseService.addRecommendationToCourse(courseId, userId, request);
    }

    @Operation(summary = "장소 추천")
    @PostMapping("/{courseId}/recommendations")
    public CourseResponse.RecommendationCreated recommendPlace(
            @PathVariable Long courseId,
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId, // 수정됨
            @Valid @RequestBody CourseRequest.RecommendPlace request
    ) {
        return courseService.recommendPlace(courseId, userId, request);
    }

    @Operation(summary = "추천 장소 댓글 달기")
    @PostMapping("/recommendations/{recommendationId}/comments")
    public CourseResponse.RecommendationCommentCreated addRecommendationComment(
            @PathVariable Long recommendationId,
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId, // 수정됨
            @Valid @RequestBody CourseRequest.RecommendationComment request
    ) {
        return courseService.addRecommendationComment(recommendationId, userId, request);
    }

    @Operation(summary = "추천 장소 리스트 조회")
    @GetMapping("/{courseId}/recommendations")
    public List<CourseResponse.Recommendation> getRecommendations(
            @PathVariable Long courseId,
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId, // 수정됨
            @RequestParam(required = false) String recommender // RecommendationSource Enum을 사용한다면 해당 타입으로 변경
    ) {
        return courseService.getRecommendations(courseId, userId, recommender);
    }
}
