package com.daytodo.domain.place.dto.response;

import lombok.Builder;

import java.util.List;

public class PlaceResDTO {

    // 저장 목록 조회
    public record GetBookmarkList(
            List<BookmarkItem> bookmarks
    ){
        @Builder
        public record BookmarkItem(
                Long bookmarkId,
                Long magazineId,
                String thumbnailUrl,
                String placeName,
                String regionName,
                String category
        ){}
    }

    // 장소 저장(북마크) 결과
    @Builder
    public record CreateBookmark(
            Long bookmarkId,
            Long placeId
    ){}

    // 장소 저장 해제 결과
    @Builder
    public record DeleteBookmark(
            Long bookmarkId,
            boolean deleted
    ){}

    // 장소 검색 조회
    public record GetPlaceSearch(
            List<PlaceItem> places
    ){
        @Builder
        public record PlaceItem(
                String placeName,
                String category,
                String regionName,
                String description
                ){}
    }
}
