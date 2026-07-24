package com.daytodo.domain.place.controller;

import com.daytodo.domain.place.dto.request.PlaceReqDTO;
import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.service.PlaceSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    private final PlaceSearchService placeSearchService;

    @GetMapping("/search")
    public PlaceResDTO.GetPlaceSearch searchPlaces(
            @ModelAttribute @Valid PlaceReqDTO.GetPlaceSearch request
    ) {
        return placeSearchService.search(request);
    }
}
