package com.daytodo.domain.course.controller;

import com.daytodo.domain.course.dto.DiaryRequest;
import com.daytodo.domain.course.dto.DiaryResponse;
import com.daytodo.domain.course.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Diary")
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(summary = "일기 작성")
    @PostMapping("/diaries")
    public DiaryResponse.Write writeDiary(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DiaryRequest.Write request
    ) {
        return diaryService.writeDiary(userId, request);
    }

    @Operation(summary = "일기 캘린더")
    @GetMapping("/diaries/calendar")
    public DiaryResponse.Calendar getCalendar(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return diaryService.getCalendar(userId, year, month);
    }

    @Operation(summary = "다녀간 코스 조회")
    @GetMapping("/diaries/{diaryId}/course")
    public DiaryResponse.VisitedCourse getVisitedCourse(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long diaryId
    ) {
        return diaryService.getVisitedCourse(userId, diaryId);
    }

    @Operation(summary = "날짜별 추억 조회")
    @GetMapping("/diaries")
    public DiaryResponse.MemoryByDate getMemoryByDate(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return diaryService.getMemoryByDate(userId, date);
    }

    @Operation(summary = "일기 사진 조회")
    @GetMapping("/{courseId}/memory-photos")
    public DiaryResponse.Photos getPhotos(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId
    ) {
        return diaryService.getPhotosByCourse(userId, courseId);
    }
}