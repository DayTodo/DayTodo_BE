package com.daytodo.domain.course.exception.code;

import com.daytodo.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DiaryErrorCode implements BaseErrorCode {

    // TODO(팀 확인 필요): PR #18 머지 후 UserErrorCode.USER_NOT_FOUND로 통합 검토 필요
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    COURSE_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "COURSE_NOT_COMPLETED", "완료된 코스에만 일기를 작성할 수 있습니다."),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY_NOT_FOUND", "일기를 찾을 수 없습니다."),
    INVALID_CALENDAR_DATE(HttpStatus.BAD_REQUEST, "INVALID_CALENDAR_DATE", "유효하지 않은 연도 또는 월입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}