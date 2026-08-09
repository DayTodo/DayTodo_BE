package com.daytodo.domain.place.service;

import com.daytodo.domain.place.converter.PlaceConverter;
import com.daytodo.domain.place.dto.request.PlaceReqDTO;
import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.infra.NaverLocalSearchClient;
import com.daytodo.domain.place.infra.NaverLocalSearchResponse;
import com.daytodo.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    // 네이버 지역검색 좌표(mapx/mapy)는 WGS84 좌표에 1e7 을 곱한 정수 문자열이다.
    private static final double NAVER_COORDINATE_SCALE = 10_000_000.0;
    private static final double MAX_LATITUDE = 90.0;    // WGS84 위도 범위 ±90
    private static final double MAX_LONGITUDE = 180.0;  // WGS84 경도 범위 ±180
    private static final String HTML_TAG_PATTERN = "<[^>]*>";

    private final NaverLocalSearchClient naverLocalSearchClient;
    private final PlaceRepository placeRepository;

    /*
     * 장소 검색
     * 네이버 지역검색 결과를 place 테이블에 lazy-upsert 하고, 내부 placeId·좌표를 포함해 응답한다.
     * 이렇게 저장된 placeId 로 추천 등록/코스 담기를 이어서 할 수 있다.
     * 좌표가 없거나 유효 범위를 벗어난 항목(지도 표시·담기 불가)은 결과에서 제외한다.
     * 항목별 upsert 는 각자 독립적이라 메서드 전체를 하나의 트랜잭션으로 묶지 않는다
     * (Spring Data 리포지토리 메서드 단위 트랜잭션 사용 → 동시성 위반 시 재조회가 안전하게 동작).
     */
    public PlaceResDTO.GetPlaceSearch search(
            PlaceReqDTO.GetPlaceSearch request
    ){
        NaverLocalSearchResponse response = naverLocalSearchClient.search(request.query());
        if (response == null || response.items() == null) {
            return new PlaceResDTO.GetPlaceSearch(List.of());
        }

        List<PlaceResDTO.GetPlaceSearch.PlaceItem> places = response.items().stream()
                .map(this::upsertPlace)
                .flatMap(Optional::stream)
                .map(PlaceConverter::toPlaceItem)
                .toList();

        return new PlaceResDTO.GetPlaceSearch(places);
    }

    // 네이버 항목을 naverPlaceId 기준으로 조회하고, 없으면 저장한다(추천 담기 플로우와 동일한 upsert).
    private Optional<Place> upsertPlace(NaverLocalSearchResponse.Item item) {
        Double latitude = coordinate(item.mapy(), MAX_LATITUDE);
        Double longitude = coordinate(item.mapx(), MAX_LONGITUDE);
        if (latitude == null || longitude == null) {
            return Optional.empty();
        }

        String naverPlaceId = externalId(item);
        Place place = placeRepository.findByNaverPlaceId(naverPlaceId)
                .orElseGet(() -> saveOrGetExisting(item, naverPlaceId, latitude, longitude));
        return Optional.of(place);
    }

    // 동시 요청이 같은 장소를 저장하면 naver_place_id 유니크 제약에 걸리므로,
    // 위반 시 먼저 저장된 행을 재조회해 반환한다(유니크 제약 위반이 500 으로 새는 것을 방지).
    private Place saveOrGetExisting(NaverLocalSearchResponse.Item item, String naverPlaceId,
                                    double latitude, double longitude) {
        try {
            return placeRepository.save(PlaceConverter.toNewPlace(item, naverPlaceId, latitude, longitude));
        } catch (DataIntegrityViolationException exception) {
            return placeRepository.findByNaverPlaceId(naverPlaceId)
                    .orElseThrow(() -> exception);
        }
    }

    // 파싱 결과가 유한하고 WGS84 허용 범위(위도 ±90, 경도 ±180) 안일 때만 좌표로 인정한다.
    // (Double.parseDouble 은 "NaN"/"Infinity" 를 예외 없이 통과시키므로 별도 검증이 필요하다.)
    private Double coordinate(String value, double absoluteBound) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value) / NAVER_COORDINATE_SCALE;
            if (!Double.isFinite(parsed) || Math.abs(parsed) > absoluteBound) {
                return null;
            }
            return parsed;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    // 네이버는 안정적인 장소 ID 를 주지 않으므로, link 가 있으면 link 를, 없으면 이름+주소 해시를 식별자로 쓴다.
    private String externalId(NaverLocalSearchResponse.Item item) {
        if (item.link() != null && !item.link().isBlank()) {
            return item.link();
        }
        String title = item.title() == null ? "" : item.title().replaceAll(HTML_TAG_PATTERN, "");
        String address = item.address() == null ? "" : item.address();
        String roadAddress = item.roadAddress() == null ? "" : item.roadAddress();
        return sha256(title + "|" + address + "|" + roadAddress);
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
