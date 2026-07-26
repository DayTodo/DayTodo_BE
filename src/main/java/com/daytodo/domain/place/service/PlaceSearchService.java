package com.daytodo.domain.place.service;

import com.daytodo.domain.place.converter.PlaceConverter;
import com.daytodo.domain.place.dto.request.PlaceReqDTO;
import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.infra.NaverLocalSearchClient;
import com.daytodo.domain.place.infra.NaverLocalSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final NaverLocalSearchClient naverLocalSearchClient;

    public PlaceResDTO.GetPlaceSearch search(
            PlaceReqDTO.GetPlaceSearch request
    ){
        NaverLocalSearchResponse response = naverLocalSearchClient.search(request.query());
        return PlaceConverter.toPlaceSearch(response);
    }
}
