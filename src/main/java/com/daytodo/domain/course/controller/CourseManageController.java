package com.daytodo.domain.course.controller;

import com.daytodo.domain.course.dto.CourseReqDto;
import com.daytodo.domain.course.dto.CourseResDto;
import com.daytodo.domain.course.service.CourseManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseManageController {

    private final CourseManageService courseManageService;


    //코스 편집
    @PatchMapping("/{courseId}/setting")
    public ResponseEntity<CourseResDto.SettingRes> updateCourseSetting(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseReqDto.SettingReq request
    ) {
        Long userId = null; // TODO: 인증 붙이면 실제 로그인 사용자 ID로 교체

        CourseResDto.SettingRes response = courseManageService.updateCourseSetting(courseId, userId, request);
        return ResponseEntity.ok(response);
    }


    //코스 장소 리스트 조회
    @GetMapping("/{courseId}/places")
    public ResponseEntity<List<CourseResDto.CoursePlaceRes>> getCoursePlaces(
            @PathVariable Long courseId
    ) {
        Long userId = null; // TODO: 인증 붙이면 실제 로그인 사용자 ID로 교체

        List<CourseResDto.CoursePlaceRes> response =
                courseManageService.getCoursePlaces(courseId, userId);
        return ResponseEntity.ok(response);
    }

    // 코스 멤버 목록 조회
    @GetMapping("/{courseId}/members")
    public ResponseEntity<List<CourseResDto.CourseMemberRes>> getCourseMembers(
            @PathVariable Long courseId
    ) {
        Long userId = null; // TODO: 인증 붙이면 실제 로그인 사용자 ID로 교체

        List<CourseResDto.CourseMemberRes> response =
                courseManageService.getCourseMembers(courseId, userId);
        return ResponseEntity.ok(response);
    }
}