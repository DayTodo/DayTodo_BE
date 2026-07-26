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
    INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST,
            "INVALID_PRICE_RANGE",
            "최소 가격은 최대 가격보다 클 수 없습니다."),
    INVALID_COURSE_ID(HttpStatus.BAD_REQUEST,
            "INVALID_COURSE_ID",
            "잘못된 코스 ID입니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "COURSE_NOT_FOUND",
            "존재하지 않는 코스입니다."),
    COURSE_DATE_CHANGE_NOT_ALLOWED(HttpStatus.CONFLICT,
            "COURSE_DATE_CHANGE_NOT_ALLOWED",
            "진행 중인 코스는 날짜를 수정할 수 없습니다."),
    COURSE_ACCESS_DENIED(HttpStatus.FORBIDDEN,
            "COURSE_ACCESS_DENIED",
            "코스에 접근할 권한이 없습니다."),
    INVALID_MEMBER(HttpStatus.BAD_REQUEST,
            "INVALID_MEMBER",
            "잘못된 멤버 요청입니다."),
    COURSE_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "COURSE_MEMBER_NOT_FOUND",
            "해당 코스에 참여 중인 멤버를 찾을 수 없습니다."),
    OWNER_CANNOT_BE_REMOVED(HttpStatus.CONFLICT,
            "OWNER_CANNOT_BE_REMOVED",
            "방장은 강퇴할 수 없습니다."),
    COURSE_SAME_DAY_EDIT_NOT_ALLOWED(HttpStatus.FORBIDDEN,
            "COURSE_SAME_DAY_EDIT_NOT_ALLOWED",
            "당일에는 코스를 수정할 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
