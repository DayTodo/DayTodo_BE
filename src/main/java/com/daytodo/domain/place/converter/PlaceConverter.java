package com.daytodo.domain.place.converter;

import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.infra.NaverLocalSearchResponse;

/**
 * Naver 지역검색 원본 응답 -> 장소 검색 응답 변환.
 */
public class PlaceConverter {

    private PlaceConverter() {
    }

    private static final String HTML_TAG_PATTERN = "<[^>]*>";
    private static final String DEFAULT_CATEGORY = "기타";

    /**
     * 네이버 지역검색 항목 -> 신규 저장용 Place.
     * 좌표/식별자(naverPlaceId)는 호출부(서비스)에서 파싱해 넘겨준다.
     * region 은 검색 시점에 알 수 없으므로 null 로 저장한다(place.region_id nullable).
     */
    public static Place toNewPlace(
            NaverLocalSearchResponse.Item item,
            String naverPlaceId,
            double latitude,
            double longitude
    ) {
        String category = normalizeCategory(item.category());
        return new Place(
                null,
                naverPlaceId,
                removeHtmlTags(item.title()),
                category != null ? category : DEFAULT_CATEGORY,
                item.address() != null ? item.address() : "",
                emptyToNull(item.roadAddress()),
                latitude,
                longitude,
                emptyToNull(item.telephone()),
                emptyToNull(removeHtmlTags(item.description())),
                null
        );
    }

    // 저장된 Place -> 장소 검색 응답 항목.
    // 검색 시 네이버 결과를 place 테이블에 upsert 하므로, 응답은 저장된 Place 기준으로 만든다.
    public static PlaceResDTO.GetPlaceSearch.PlaceItem toPlaceItem(Place place) {
        return PlaceResDTO.GetPlaceSearch.PlaceItem.builder()
                .placeId(place.getPlaceId())
                .placeName(place.getPlaceName())
                .category(place.getCategory())
                .regionName(RegionNameResolver.fromAddress(place.getRoadAddress(), place.getAddress()))
                .description(emptyToNull(place.getDescription()))
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
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
}
