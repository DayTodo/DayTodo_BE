package com.daytodo.domain.place.converter;

import java.util.Map;

/**
 * 주소 문자열에서 응답용 지역명("시 구")을 추출하는 공용 로직.
 * 장소의 region(FK)이 없을 때(네이버 검색 출처 등) 주소로 지역명을 만들기 위해 사용한다.
 * (검색 응답과 저장 목록 응답이 동일한 규칙으로 지역명을 만들도록 한 곳에 모아둔다.)
 */
public final class RegionNameResolver {

    private RegionNameResolver() {
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

    /**
     * 도로명 주소 우선, 없으면 지번 주소에서 "시도(축약) 시군구" 형태의 지역명을 추출한다.
     * 주소가 없거나 토큰이 2개 미만이면 null.
     */
    public static String fromAddress(String roadAddress, String address) {
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
