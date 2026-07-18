package com.daytodo.domain.course.controller;

import com.daytodo.domain.course.dto.CourseReqDto;
import com.daytodo.domain.course.dto.CourseResDto;
import com.daytodo.domain.course.service.CourseManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}