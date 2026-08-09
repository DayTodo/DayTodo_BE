package com.daytodo.domain.place.converter;

import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.infra.NaverLocalSearchResponse;

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
                .regionName(extractRegionName(place.getRoadAddress(), place.getAddress()))
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
