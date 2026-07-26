package com.daytodo.domain.place.infra;

import java.util.List;

/**
 * Naver 지역검색 API 원본 응답.
 * 필드명은 Naver 응답 JSON 키와 일치해야 함 (Jackson 역직렬화)
 */
public record NaverLocalSearchResponse(
        int total,
        int start,
        int display,
        List<Item> items
) {
    public record Item(
            String title,        // 검색어와 일치하는 구간에 <b> 태그가 포함됨
            String link,
            String category,     // "카페,디저트" 형태
            String description,
            String telephone,
            String address,
            String roadAddress,
            String mapx,         // Naver가 문자열로 내려줌
            String mapy
    ) {}
}
