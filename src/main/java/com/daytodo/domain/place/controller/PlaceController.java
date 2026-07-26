package com.daytodo.domain.place.controller;

import com.daytodo.domain.place.dto.request.PlaceReqDTO;
import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.service.PlaceBookmarkService;
import com.daytodo.domain.place.service.PlaceSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    private final PlaceSearchService placeSearchService;
    private final PlaceBookmarkService placeBookmarkService;

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
}
