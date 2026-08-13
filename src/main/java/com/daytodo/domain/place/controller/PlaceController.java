package com.daytodo.domain.place.controller;

import com.daytodo.domain.place.dto.request.PlaceReqDTO;
import com.daytodo.domain.place.dto.response.MagazineResDTO;
import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.service.MagazineService;
import com.daytodo.domain.place.service.PlaceBookmarkService;
import com.daytodo.domain.place.service.PlaceSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Place")
@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    private final PlaceSearchService placeSearchService;
    private final PlaceBookmarkService placeBookmarkService;
    private final MagazineService magazineService;

    @Operation(summary = "장소 검색")
    @GetMapping("/search")
    public PlaceResDTO.GetPlaceSearch searchPlaces(
            @ParameterObject @ModelAttribute @Valid PlaceReqDTO.GetPlaceSearch request
    ) {
        return placeSearchService.search(request);
    }

    @Operation(summary = "저장 목록 조회")
    @GetMapping("/bookmarks")
    public PlaceResDTO.GetBookmarkList getBookmarkList(
            @AuthenticationPrincipal Long userId,
            @ParameterObject @ModelAttribute PlaceReqDTO.GetBookmarkList request
    ) {
        return placeBookmarkService.getBookmarkList(userId, request);
    }

    @Operation(summary = "오늘의 Pick 매거진 목록 조회")
    @GetMapping("/magazines")
    public MagazineResDTO.GetMagazineList getMagazines(
            @AuthenticationPrincipal Long userId
    ) {
        return magazineService.getMagazineList(userId);
    }

    @Operation(summary = "매거진 상세 조회")
    @GetMapping("/magazines/{magazineId}")
    public MagazineResDTO.GetMagazineDetail getMagazineDetail(
            @PathVariable Long magazineId
    ) {
        return magazineService.getMagazineDetail(magazineId);
    }

    @Operation(summary = "매거진 사진 더보기")
    @GetMapping("/magazines/{magazineId}/photos")
    public MagazineResDTO.GetMagazinePhotos getMagazinePhotos(
            @PathVariable Long magazineId
    ) {
        return magazineService.getMagazinePhotos(magazineId);
    }

    @Operation(
            summary = "매거진 장소 저장",
            description = "매거진 상세 조회 응답의 magazineId 값을 contentId 로 그대로 전달한다. "
                    + "(contentId = 관광 KorService2 콘텐츠 ID, 내부 Place PK 아님)"
    )
    @PostMapping("/bookmarks")
    public PlaceResDTO.CreateBookmark createBookmark(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PlaceReqDTO.CreateBookmark request
    ) {
        return placeBookmarkService.createBookmark(userId, request.contentId());
    }

    @Operation(summary = "코스 장소 저장 (기록 탭)")
    @PostMapping("/{placeId}/bookmarks")
    public PlaceResDTO.CreateBookmark createBookmarkByPlaceId(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long placeId
    ) {
        return placeBookmarkService.createBookmarkByPlaceId(userId, placeId);
    }

    @Operation(summary = "매거진 장소 저장 해제")
    @DeleteMapping("/bookmarks/{bookmarkId}")
    public PlaceResDTO.DeleteBookmark deleteBookmark(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookmarkId
    ) {
        return placeBookmarkService.deleteBookmark(userId, bookmarkId);
    }
}