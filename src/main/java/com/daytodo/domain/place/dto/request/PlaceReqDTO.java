package com.daytodo.domain.place.dto.request;

import com.daytodo.domain.place.enums.BookmarkSortType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public class PlaceReqDTO {

    // 저장 목록 조회
    public record GetBookmarkList(
        BookmarkSortType sort,
        Long regionId
    ){}

    // 장소 검색 조회
    public record GetPlaceSearch(
        @NotBlank(message = "검색어를 입력해주세요.") String query
    ){}
}
