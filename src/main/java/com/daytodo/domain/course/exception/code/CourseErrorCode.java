package com.daytodo.domain.course.exception.code;

import com.daytodo.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements BaseErrorCode {

    INVALID_COURSE_REQUEST(HttpStatus.BAD_REQUEST,
            "INVALID_COURSE_REQUEST",
            "코스 수정 요청값이 올바르지 않습니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "COURSE_NOT_FOUND",
            "존재하지 않는 코스입니다."),
    COURSE_DATE_CHANGE_NOT_ALLOWED(HttpStatus.CONFLICT,
            "COURSE_DATE_CHANGE_NOT_ALLOWED",
            "진행 중인 코스는 날짜를 수정할 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}