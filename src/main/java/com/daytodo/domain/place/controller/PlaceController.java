package com.daytodo.domain.place.controller;

import com.daytodo.domain.place.dto.request.PlaceReqDTO;
import com.daytodo.domain.place.dto.response.MagazineResDTO;
import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.service.MagazineService;
import com.daytodo.domain.place.service.PlaceBookmarkService;
import com.daytodo.domain.place.service.PlaceSearchService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    private final PlaceSearchService placeSearchService;
    private final PlaceBookmarkService placeBookmarkService;
    private final MagazineService magazineService;

    @GetMapping("/search")
    public PlaceResDTO.GetPlaceSearch searchPlaces(
            @ParameterObject @ModelAttribute @Valid PlaceReqDTO.GetPlaceSearch request
    ) {
        return placeSearchService.search(request);
    }

    // TODO: JWT 인증 적용 후 @AuthenticationPrincipal 로 교체
    @GetMapping("/bookmarks")
    public PlaceResDTO.GetBookmarkList getBookmarkList(
            @RequestHeader("X-User-Id") Long userId,
            @ParameterObject @ModelAttribute PlaceReqDTO.GetBookmarkList request
    ) {
        return placeBookmarkService.getBookmarkList(userId, request);
    }

    // 오늘의 Pick 매거진 목록 (HOM-004)
    @GetMapping("/magazines")
    public MagazineResDTO.GetMagazineList getMagazines(
            @AuthenticationPrincipal Long userId
    ) {
        return magazineService.getMagazineList(userId);
    }

    // 매거진 상세
    @GetMapping("/magazines/{magazineId}")
    public MagazineResDTO.GetMagazineDetail getMagazineDetail(
            @PathVariable Long magazineId
    ) {
        return magazineService.getMagazineDetail(magazineId);
    }

    // 매거진 사진 목록 (MAG-003)
    @GetMapping("/magazines/{magazineId}/photos")
    public MagazineResDTO.GetMagazinePhotos getMagazinePhotos(
            @PathVariable Long magazineId
    ) {
        return magazineService.getMagazinePhotos(magazineId);
    }

    // 장소 저장(북마크) (MAG-004 / SAV-001)
    @PostMapping("/bookmarks")
    public PlaceResDTO.CreateBookmark createBookmark(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PlaceReqDTO.CreateBookmark request
    ) {
        return placeBookmarkService.createBookmark(userId, request.placeId());
    }

    // 장소 저장 해제
    @DeleteMapping("/bookmarks/{bookmarkId}")
    public PlaceResDTO.DeleteBookmark deleteBookmark(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookmarkId
    ) {
        return placeBookmarkService.deleteBookmark(userId, bookmarkId);
    }
}
