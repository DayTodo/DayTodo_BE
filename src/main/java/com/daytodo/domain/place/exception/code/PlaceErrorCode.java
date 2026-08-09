package com.daytodo.domain.place.exception.code;

import com.daytodo.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements BaseErrorCode {

    // 장소 검색: Naver 지역 검색 API 호출 실패
    NAVER_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "일시적인 오류가 발생했어요. 잠시 후 다시 시도해주세요."),

    // 매거진: 한국관광공사 KorService2 호출 실패
    TOUR_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "일시적인 오류가 발생했어요. 잠시 후 다시 시도해주세요."),

    // 매거진: 존재하지 않는 매거진(관광 콘텐츠)
    MAGAZINE_NOT_FOUND(HttpStatus.NOT_FOUND, "MAGAZINE_NOT_FOUND", "매거진을 찾을 수 없습니다."),

    // 북마크: 이미 저장된 장소
    DUPLICATE_BOOKMARK(HttpStatus.CONFLICT, "DUPLICATE_BOOKMARK", "이미 저장된 장소입니다."),

    // 북마크: 저장된 장소 없음(해제 대상 없음)
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKMARK_NOT_FOUND", "저장된 장소를 찾을 수 없습니다."),

    // 북마크: placeId로 조회했는데 해당 장소가 존재하지 않음
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_NOT_FOUND", "장소를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
