package com.daytodo.domain.place.infra;

import com.daytodo.domain.place.exception.code.PlaceErrorCode;
import com.daytodo.global.apiPayload.exception.ProjectException;
import com.daytodo.global.config.TourApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

/**
 * 한국관광공사 KorService2 호출 담당.
 * 응답 가공 없이 원본 item 리스트만 반환한다 (가공은 MagazineConverter/Service 책임).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TourApiClient {

    private static final String AREA_BASED_LIST = "/areaBasedList2";
    private static final String DETAIL_COMMON = "/detailCommon2";
    private static final String DETAIL_INTRO = "/detailIntro2";
    private static final String DETAIL_IMAGE = "/detailImage2";

    private final RestClient tourRestClient;
    private final TourApiProperties tourApiProperties;

    /** 지역기반 관광정보 목록. areaCode/sigunguCode 가 null 이면 전국. */
    public List<TourApiResponse.AreaItem> areaBasedList(Integer areaCode, Integer sigunguCode, int numOfRows) {
        URI uri = buildUri(AREA_BASED_LIST, b -> {
            b.queryParam("numOfRows", numOfRows);
            b.queryParam("pageNo", 1);
            b.queryParam("arrange", "O"); // 대표이미지 있는 제목순
            if (areaCode != null) {
                b.queryParam("areaCode", areaCode);
            }
            if (sigunguCode != null) {
                b.queryParam("sigunguCode", sigunguCode);
            }
        });
        return call(uri, new ParameterizedTypeReference<TourApiResponse<TourApiResponse.AreaItem>>() {}).items();
    }

    /** 공통정보(제목·주소·개요 등). 없으면 null. */
    public TourApiResponse.CommonItem detailCommon(String contentId) {
        URI uri = buildUri(DETAIL_COMMON, b -> b.queryParam("contentId", contentId));
        List<TourApiResponse.CommonItem> items =
                call(uri, new ParameterizedTypeReference<TourApiResponse<TourApiResponse.CommonItem>>() {}).items();
        return items.isEmpty() ? null : items.get(0);
    }

    /** 소개정보(영업시간 등). contentTypeId 필수. 없으면 null. */
    public TourApiResponse.IntroItem detailIntro(String contentId, String contentTypeId) {
        URI uri = buildUri(DETAIL_INTRO, b -> {
            b.queryParam("contentId", contentId);
            b.queryParam("contentTypeId", contentTypeId);
        });
        List<TourApiResponse.IntroItem> items =
                call(uri, new ParameterizedTypeReference<TourApiResponse<TourApiResponse.IntroItem>>() {}).items();
        return items.isEmpty() ? null : items.get(0);
    }

    /** 이미지정보(콘텐츠 사진). image_order 정렬은 호출부 책임. */
    public List<TourApiResponse.ImageItem> detailImage(String contentId) {
        URI uri = buildUri(DETAIL_IMAGE, b -> {
            b.queryParam("contentId", contentId);
            b.queryParam("imageYN", "Y");
            b.queryParam("numOfRows", 30);
            b.queryParam("pageNo", 1);
        });
        return call(uri, new ParameterizedTypeReference<TourApiResponse<TourApiResponse.ImageItem>>() {}).items();
    }

    private <T> TourApiResponse<T> call(URI uri, ParameterizedTypeReference<TourApiResponse<T>> type) {
        try {
            return tourRestClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(type);
        } catch (RestClientException e) {
            log.error("KorService2 호출 실패. uri={}", uri.getPath(), e);
            throw new ProjectException(PlaceErrorCode.TOUR_API_ERROR);
        }
    }

    /**
     * 공통 파라미터(serviceKey, MobileOS, MobileApp, _type)를 붙인 절대 URI 생성.
     * serviceKey 는 이미 인코딩된 키이므로 build(true) 로 이중 인코딩을 막는다.
     * (그 외 파라미터 값은 영숫자라 재인코딩이 필요 없다.)
     */
    private URI buildUri(String path, Consumer<UriComponentsBuilder> params) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(tourApiProperties.baseUrl() + path)
                .queryParam("serviceKey", tourApiProperties.serviceKey())
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "daytodo")
                .queryParam("_type", "json");
        params.accept(builder);
        return builder.build(true).toUri();
    }
}
