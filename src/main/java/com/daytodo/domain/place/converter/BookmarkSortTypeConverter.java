package com.daytodo.domain.place.converter;

import com.daytodo.domain.place.enums.BookmarkSortType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 쿼리 파라미터의 정렬 값(recent, oldest ...)을 BookmarkSortType 으로 변환
 * 기본 enum 바인딩은 대소문자를 구분하므로 소문자 표기를 받기 위해 등록
 */
@Component
public class BookmarkSortTypeConverter implements Converter<String, BookmarkSortType> {

    @Override
    public BookmarkSortType convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        // 정의되지 않은 값이면 IllegalArgumentException 이 발생하고 400 으로 처리
        return BookmarkSortType.valueOf(source.trim().toUpperCase());
    }
}
