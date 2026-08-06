package com.daytodo.domain.course.exception.code;

import com.daytodo.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

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

    // 코스 종료
    INVALID_COURSE_STATUS(HttpStatus.CONFLICT, "INVALID_COURSE_STATUS", "진행 중인 코스만 종료할 수 있습니다."),

    // 추억 사진 저장
    EMPTY_MEMORY_PHOTO(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "저장할 이미지가 없습니다."),

    COURSE_SAME_DAY_EDIT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COURSE_SAME_DAY_EDIT_NOT_ALLOWED", "당일 코스는 수정할 수 없습니다."),
    COURSE_DATE_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COURSE_DATE_CHANGE_NOT_ALLOWED", "진행 중인 코스의 날짜는 변경할 수 없습니다."),
    COURSE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COURSE_ACCESS_DENIED", "코스에 대한 접근 권한이 없습니다."),
    COURSE_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_MEMBER_NOT_FOUND", "해당 코스 멤버를 찾을 수 없습니다."),
    OWNER_CANNOT_BE_REMOVED(HttpStatus.BAD_REQUEST, "OWNER_CANNOT_BE_REMOVED", "방장은 강퇴할 수 없습니다."),

    // ==============================================================================
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMENDATION_NOT_FOUND", "추천 정보를 찾을 수 없습니다."),
    RECOMMENDATION_ALREADY_LIKED(HttpStatus.CONFLICT, "RECOMMENDATION_ALREADY_LIKED", "이미 좋아요를 누른 추천입니다."),
    DUPLICATE_PLACE(HttpStatus.CONFLICT, "DUPLICATE_PLACE", "이미 코스에 추가된 장소입니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_NOT_FOUND", "장소를 찾을 수 없습니다."),
    DUPLICATE_RECOMMENDATION(HttpStatus.CONFLICT, "DUPLICATE_RECOMMENDATION", "이미 추천된 장소입니다."),

    // 코스 장소 추가
    MISSING_PLACE_ID(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "placeId가 필요합니다."),

    // 코스 장소 순서 변경
    INVALID_PLACE_ORDER(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "순서 정보가 올바르지 않습니다."),
    COURSE_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_PLACE_NOT_FOUND", "코스에 속하지 않는 장소가 포함되어 있습니다.");
    COURSE_RECOMMENDATION_FAILED(HttpStatus.CONFLICT, "COURSE_RECOMMENDATION_FAILED", "추천 코스를 생성할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
