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
                .magazineId(null)   // 매거진 도메인 연동 전까지 null
                .thumbnailUrl(place.getImageUrl())
                .placeName(place.getPlaceName())
                .regionName(toRegionName(place.getRegion()))
                .category(place.getCategory())
                .build();
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
