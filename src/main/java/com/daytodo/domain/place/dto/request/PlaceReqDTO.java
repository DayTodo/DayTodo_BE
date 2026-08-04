package com.daytodo.domain.place.dto.request;

import com.daytodo.domain.place.enums.BookmarkSortType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class PlaceReqDTO {

    // 저장 목록 조회
    public record GetBookmarkList(
        BookmarkSortType sort,
        Long regionId
    ){}

    // 장소 저장(북마크). contentId = 관광(KorService2) 콘텐츠 ID (내부 Place PK 아님)
    public record CreateBookmark(
        @NotNull(message = "contentId가 필요합니다.") Long contentId
    ){}

    // 장소 검색 조회
    public record GetPlaceSearch(
        @NotBlank(message = "검색어를 입력해주세요.") String query
    ){}
}
