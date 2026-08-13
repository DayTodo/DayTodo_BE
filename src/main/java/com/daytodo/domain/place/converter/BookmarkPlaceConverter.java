package com.daytodo.domain.place.converter;

import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.entity.mapping.BookmarkPlace;
import com.daytodo.domain.region.entity.Region;

import java.util.List;

/**
 * BookmarkPlace -> 저장 목록 조회 응답 변환.
 */
public class BookmarkPlaceConverter {

    private BookmarkPlaceConverter() {
    }

    public static PlaceResDTO.GetBookmarkList toBookmarkList(List<BookmarkPlace> bookmarkPlaces) {
        List<PlaceResDTO.GetBookmarkList.BookmarkItem> bookmarks = bookmarkPlaces.stream()
                .map(BookmarkPlaceConverter::toBookmarkItem)
                .toList();

        return new PlaceResDTO.GetBookmarkList(bookmarks);
    }

    private static PlaceResDTO.GetBookmarkList.BookmarkItem toBookmarkItem(BookmarkPlace bookmarkPlace) {
        Place place = bookmarkPlace.getPlace();

        return PlaceResDTO.GetBookmarkList.BookmarkItem.builder()
                .bookmarkId(bookmarkPlace.getId())
                .placeId(place.getPlaceId())
                // 관광(KorService2) 출처 장소는 tour_content_id 가 곧 magazineId(=contentId).
                // 네이버 출처 장소는 값이 없어 null (해당 항목은 매거진 상세로 이동 불가).
                .magazineId(parseContentId(place.getTourContentId()))
                .thumbnailUrl(place.getImageUrl())
                .placeName(place.getPlaceName())
                .regionName(resolveRegionName(place))
                .category(place.getCategory())
                .build();
    }

    // region(FK)이 있으면 그 지역명을, 없으면(네이버 검색 출처 등 미매핑) 주소 문자열로 지역명을 만든다.
    // (검색 응답은 처음부터 주소로 지역명을 만드는데, region 미매핑 장소는 저장 목록에서 지역명이
    //  비어 보이던 문제가 있어 동일한 주소 기반 규칙으로 fallback 한다.)
    private static String resolveRegionName(Place place) {
        String fromRegion = toRegionName(place.getRegion());
        if (fromRegion != null) {
            return fromRegion;
        }
        return RegionNameResolver.fromAddress(place.getRoadAddress(), place.getAddress());
    }

    // tour_content_id(숫자 문자열) -> Long. 값이 없거나 숫자가 아니면 null 로 흡수.
    private static Long parseContentId(String tourContentId) {
        if (tourContentId == null || tourContentId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(tourContentId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // "시 구" 형태로 조립한다. 시/도 단위 지역이면 상위 지역이 없으므로 지역명만 반환
    private static String toRegionName(Region region) {
        if (region == null) {
            return null;
        }

        Region parent = region.getParent();
        if (parent == null) {
            return region.getRegionName();
        }

        return parent.getRegionName() + " " + region.getRegionName();
    }
}
