package com.daytodo.domain.place.service;

import com.daytodo.domain.place.dto.request.PlaceReqDTO;
import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.infra.NaverLocalSearchClient;
import com.daytodo.domain.place.infra.NaverLocalSearchResponse;
import com.daytodo.domain.place.repository.PlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceSearchServiceTest {

    @Mock NaverLocalSearchClient naverLocalSearchClient;
    @Mock PlaceRepository placeRepository;

    private PlaceSearchService service() {
        return new PlaceSearchService(naverLocalSearchClient, placeRepository);
    }

    // mapx=경도, mapy=위도. 좌표는 WGS84 * 1e7 정수 문자열.
    private NaverLocalSearchResponse.Item item(String link, String mapx, String mapy) {
        return new NaverLocalSearchResponse.Item(
                "테스트 장소", link, "카페", "설명", "02-000-0000",
                "서울특별시 강남구 어딘가", "서울특별시 강남구 테헤란로 1", mapx, mapy);
    }

    @Test
    void excludesItemsWithNonFiniteOrOutOfRangeCoordinates() {
        NaverLocalSearchResponse response = new NaverLocalSearchResponse(5, 1, 5, List.of(
                item("valid-link", "1270276000", "374979000"), // 127.0276, 37.4979 (정상)
                item("nan-link", "NaN", "374979000"),           // 경도 NaN
                item("inf-link", "1e400", "374979000"),         // 경도 Infinity (parseDouble 오버플로)
                item("lng-oob-link", "9000000000", "374979000"),// 경도 900.0 (>180)
                item("lat-oob-link", "1270276000", "1000000000")// 위도 100.0 (>90)
        ));
        when(naverLocalSearchClient.search(anyString())).thenReturn(response);
        when(placeRepository.findByNaverPlaceId("valid-link")).thenReturn(Optional.empty());
        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaceResDTO.GetPlaceSearch result = service().search(new PlaceReqDTO.GetPlaceSearch("카페"));

        // 정상 좌표 1건만 남아야 한다.
        assertThat(result.places()).hasSize(1);
        assertThat(result.places().get(0).latitude()).isEqualTo(37.4979);
        assertThat(result.places().get(0).longitude()).isEqualTo(127.0276);
    }

    @Test
    void refetchesExistingPlaceWhenConcurrentSaveHitsUniqueConstraint() {
        NaverLocalSearchResponse response = new NaverLocalSearchResponse(1, 1, 1, List.of(
                item("dup-link", "1270276000", "374979000")));
        Place existing = new Place(null, "dup-link", "테스트 장소", "카페",
                "서울특별시 강남구 어딘가", "서울특별시 강남구 테헤란로 1", 37.4979, 127.0276, null, null, null);
        ReflectionTestUtils.setField(existing, "placeId", 42L);

        when(naverLocalSearchClient.search(anyString())).thenReturn(response);
        // 최초 조회는 없음 → 저장 시도 → 동시 저장으로 유니크 위반 → 재조회 시 기존 행 반환
        when(placeRepository.findByNaverPlaceId("dup-link"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(placeRepository.save(any(Place.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate naver_place_id"));

        PlaceResDTO.GetPlaceSearch result = service().search(new PlaceReqDTO.GetPlaceSearch("카페"));

        // 예외가 새지 않고, 먼저 저장된 장소로 응답해야 한다.
        assertThat(result.places()).hasSize(1);
        assertThat(result.places().get(0).placeId()).isEqualTo(42L);
    }
}
