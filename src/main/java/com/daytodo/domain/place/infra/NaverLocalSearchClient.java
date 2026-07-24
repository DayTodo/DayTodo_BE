package com.daytodo.domain.place.infra;

import com.daytodo.domain.place.exception.code.PlaceErrorCode;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Naver 지역검색 API 호출 담당.
 * 응답 가공 없이 원본 그대로 반환 (가공은 PlaceConverter 책임)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverLocalSearchClient {

    private static final String LOCAL_SEARCH_PATH = "/v1/search/local.json";
    private static final int DISPLAY_COUNT = 5;   // 지역검색 API가 허용하는 최대값

    private final RestClient naverRestClient;

    public NaverLocalSearchResponse search(String query) {
        try {
            return naverRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(LOCAL_SEARCH_PATH)
                            .queryParam("query", query)
                            .queryParam("display", DISPLAY_COUNT)
                            .build())
                    .retrieve()
                    .body(NaverLocalSearchResponse.class);
        } catch (RestClientException e) {
            log.error("Naver 지역검색 API 호출 실패. query={}", query, e);
            throw new ProjectException(PlaceErrorCode.NAVER_API_ERROR);
        }
    }
}
