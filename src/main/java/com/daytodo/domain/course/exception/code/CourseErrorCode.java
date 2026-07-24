package com.daytodo.domain.course.exception.code;

import com.daytodo.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * TODO(팀 확인 필요): PR #18(user-course-api)의 CourseErrorCode 를 그대로 가져온 파일입니다.
 * PR #18이 develop에 머지되면 diff 확인 후 정리하면 됩니다.
 */
@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements BaseErrorCode {
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "코스를 찾을 수 없습니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "INVALID_INVITE_CODE", "올바르지 않은 초대코드입니다."),
    EXPIRED_INVITE_CODE(HttpStatus.BAD_REQUEST, "EXPIRED_INVITE_CODE", "만료된 초대코드입니다."),
    ALREADY_COURSE_MEMBER(HttpStatus.CONFLICT, "ALREADY_COURSE_MEMBER", "이미 참가한 코스입니다."),
    COURSE_NOT_JOINABLE(HttpStatus.CONFLICT, "COURSE_NOT_JOINABLE", "참가할 수 없는 코스입니다."),
    INVALID_COURSE_PERIOD(HttpStatus.BAD_REQUEST, "INVALID_COURSE_PERIOD", "조회 시작일은 종료일보다 늦을 수 없습니다."),
    INVALID_CALENDAR_DATE(HttpStatus.BAD_REQUEST, "INVALID_CALENDAR_DATE", "유효하지 않은 연도 또는 월입니다."),
    PAST_COURSE_DATE(HttpStatus.BAD_REQUEST, "PAST_COURSE_DATE", "오늘 이전 날짜로 코스를 생성할 수 없습니다."),
    INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "INVALID_PRICE_RANGE", "최대 금액은 최소 금액보다 작을 수 없습니다."),
    INVITE_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "INVITE_CODE_GENERATION_FAILED", "초대코드를 생성하지 못했습니다."),

    // 코스 종료(TDY-007)
    INVALID_COURSE_STATUS(HttpStatus.CONFLICT, "INVALID_COURSE_STATUS", "진행 중인 코스만 종료할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
