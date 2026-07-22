package com.daytodo.domain.region.exception.code;

import com.daytodo.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RegionErrorCode implements BaseErrorCode {
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "REGION_NOT_FOUND", "존재하지 않는 지역입니다."),
    UNSUPPORTED_REGION(HttpStatus.BAD_REQUEST, "UNSUPPORTED_REGION", "현재 지원하지 않는 지역입니다."),
    DUPLICATE_REGION(HttpStatus.BAD_REQUEST, "DUPLICATE_REGION", "중복된 관심지역이 포함되어 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
