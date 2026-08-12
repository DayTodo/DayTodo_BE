package com.daytodo.domain.place.converter;

import com.daytodo.domain.place.dto.response.MagazineResDTO;
import com.daytodo.domain.place.infra.TourApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * KorService2 원본 응답 -> 매거진 응답 변환.
 */
public class MagazineConverter {

    private MagazineConverter() {
    }

    private static final String HTML_TAG_PATTERN = "<[^>]*>";

    // Naver 주소 표기와 동일하게 시/도 축약 (예: 서울특별시 -> 서울)
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

    // KorService2 contentTypeId -> 표시용 카테고리명
    private static final Map<String, String> CONTENT_TYPE_NAMES = Map.of(
            "12", "관광지",
            "14", "문화시설",
            "15", "축제공연행사",
            "25", "여행코스",
            "28", "레포츠",
            "32", "숙박",
            "38", "쇼핑",
            "39", "음식점"
    );

    // ---- 목록 ----

    public static MagazineResDTO.GetMagazineList.MagazineItem toMagazineItem(
            TourApiResponse.AreaItem item,
            boolean isAd,
            String tagline
    ) {
        return MagazineResDTO.GetMagazineList.MagazineItem.builder()
                .magazineId(parseLong(item.contentid()))
                .thumbnailUrl(firstNonBlank(item.firstimage(), item.firstimage2()))
                .placeName(removeHtmlTags(item.title()))
                .regionName(extractRegionName(item.addr1()))
                .tagline(emptyToNull(removeHtmlTags(tagline)))
                .isAd(isAd)
                .build();
    }

    // ---- 상세 ----

    public static MagazineResDTO.GetMagazineDetail toDetail(
            TourApiResponse.CommonItem common,
            TourApiResponse.IntroItem intro,
            List<TourApiResponse.ImageItem> images
    ) {
        return MagazineResDTO.GetMagazineDetail.builder()
                .placeId(parseLong(common.contentid()))
                .thumbnailUrl(firstNonBlank(common.firstimage(), common.firstimage2()))
                .category(categoryName(common.contenttypeid()))
                .placeName(removeHtmlTags(common.title()))
                .address(joinAddress(common.addr1(), common.addr2()))
                .businessHours(businessHours(intro))
                .phone(emptyToNull(common.tel()))
                .content(emptyToNull(removeHtmlTags(common.overview())))
                .photos(toPhotoItems(images).stream().limit(3).toList()) // 미리보기 앞 3장
                .build();
    }

    // ---- 사진 ----

    public static MagazineResDTO.GetMagazinePhotos toPhotos(List<TourApiResponse.ImageItem> images) {
        return new MagazineResDTO.GetMagazinePhotos(toPhotoItems(images));
    }

    public static List<MagazineResDTO.PhotoItem> toPhotoItems(List<TourApiResponse.ImageItem> images) {
        if (images == null) {
            return List.of();
        }
        // serialnum 은 "2871004_3" 같은 문자열이라 숫자 정렬 불가.
        // detailImage2 응답 순서를 그대로 두고 imageOrder 를 1부터 부여한다.
        List<MagazineResDTO.PhotoItem> result = new ArrayList<>();
        int order = 1;
        for (TourApiResponse.ImageItem img : images) {
            result.add(MagazineResDTO.PhotoItem.builder()
                    .imageId(emptyToNull(img.serialnum()))
                    .imageUrl(firstNonBlank(img.originimgurl(), img.smallimageurl()))
                    .imageOrder(order++)
                    .build());
        }
        return result;
    }

    /** KorService2 contentTypeId -> 표시용 카테고리명 (미매핑 시 null). */
    public static String categoryName(String contentTypeId) {
        return contentTypeId == null ? null : CONTENT_TYPE_NAMES.get(contentTypeId);
    }

    // ---- 헬퍼 ----

    // contentTypeId 별로 영업시간 필드가 달라 해당 필드를 골라 반환
    private static String businessHours(TourApiResponse.IntroItem intro) {
        if (intro == null) {
            return null;
        }
        String value = switch (intro.contenttypeid() == null ? "" : intro.contenttypeid()) {
            case "39" -> intro.opentimefood();
            case "12" -> intro.usetime();
            case "14" -> intro.usetimeculture();
            case "28" -> intro.usetimeleports();
            case "38" -> intro.opentime();
            default -> null;
        };
        return emptyToNull(removeHtmlTags(value));
    }

    private static String joinAddress(String addr1, String addr2) {
        String base = addr1 == null ? "" : addr1.trim();
        if (addr2 != null && !addr2.isBlank()) {
            base = (base + " " + addr2.trim()).trim();
        }
        return emptyToNull(base);
    }

    // 주소 첫 두 토큰으로 "서울 성동구" 형태 지역명 추출
    private static String extractRegionName(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        String[] tokens = address.trim().split("\\s+");
        if (tokens.length < 2) {
            return null;
        }
        String sido = SIDO_SHORT_NAMES.getOrDefault(tokens[0], tokens[0]);
        return sido + " " + tokens[1];
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return emptyToNull(b);
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static String removeHtmlTags(String value) {
        return value == null ? null : value.replaceAll(HTML_TAG_PATTERN, "");
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
