package com.daytodo.domain.place.converter;

import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.infra.NaverLocalSearchResponse;

import java.util.List;
import java.util.Map;

/**
 * Naver 지역검색 원본 응답 -> 장소 검색 응답 변환.
 */
public class PlaceConverter {

    private PlaceConverter() {
    }

    // Naver 주소의 시/도 표기를 응답용 축약 표기로 변환 (예: 서울특별시 -> 서울)
    private static final Map<String, String> SIDO_SHORT_NAMES = Map.ofEntries(
            Map.entry("서울특별시", "서울"),
            Map.entry("부산광역시", "부산"),
            Map.entry("대구광역시", "대구"),
            Map.entry("인천광역시", "인천"),
            Map.entry("광주광역시", "광주"),
            Map.entry("대전광역시", "대전"),
            Map.entry("울산광역시", "울산"),
            Map.entry("세종특별자치시", "세종"),
            Map.entry("경기도", "경기"),
            Map.entry("강원특별자치도", "강원"),
            Map.entry("충청북도", "충북"),
            Map.entry("충청남도", "충남"),
            Map.entry("전북특별자치도", "전북"),
            Map.entry("전라남도", "전남"),
            Map.entry("경상북도", "경북"),
            Map.entry("경상남도", "경남"),
            Map.entry("제주특별자치도", "제주")
    );

    private static final String HTML_TAG_PATTERN = "<[^>]*>";

    public static PlaceResDTO.GetPlaceSearch toPlaceSearch(NaverLocalSearchResponse response) {
        if (response == null || response.items() == null) {
            return new PlaceResDTO.GetPlaceSearch(List.of());
        }

        List<PlaceResDTO.GetPlaceSearch.PlaceItem> places = response.items().stream()
                .map(PlaceConverter::toPlaceItem)
                .toList();

        return new PlaceResDTO.GetPlaceSearch(places);
    }

    private static PlaceResDTO.GetPlaceSearch.PlaceItem toPlaceItem(NaverLocalSearchResponse.Item item) {
        return PlaceResDTO.GetPlaceSearch.PlaceItem.builder()
                .placeName(removeHtmlTags(item.title()))
                .category(normalizeCategory(item.category()))
                .regionName(extractRegionName(item.roadAddress(), item.address()))
                .description(emptyToNull(removeHtmlTags(item.description())))
                .build();
    }

    // Naver가 값을 제공하지 않으면 빈 문자열로 내려주므로 null로 통일한다.
    private static String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    // 검색어와 일치하는 구간에 <b> 태그가 포함되어 오므로 제거
    private static String removeHtmlTags(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll(HTML_TAG_PATTERN, "");
    }

    // Naver 카테고리 구분자를 응답 표기("상위 > 하위")로 변환
    private static String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        return category.replace(">", " > ").replaceAll("\\s+", " ").trim();
    }

    // 주소에서 "시 구" 형태의 지역명을 추출 (도로명 주소 우선, 없으면 지번 주소)
    private static String extractRegionName(String roadAddress, String address) {
        String source = (roadAddress != null && !roadAddress.isBlank()) ? roadAddress : address;
        if (source == null || source.isBlank()) {
            return null;
        }

        String[] tokens = source.trim().split("\\s+");
        if (tokens.length < 2) {
            return null;
        }

        String sido = SIDO_SHORT_NAMES.getOrDefault(tokens[0], tokens[0]);
        return sido + " " + tokens[1];
    }
}
