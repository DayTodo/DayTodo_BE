package com.daytodo.domain.course.service;

import com.daytodo.domain.course.dto.CourseRequest;
import com.daytodo.domain.course.dto.CourseResponse;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.entity.PlacePriceEstimate;
import com.daytodo.domain.place.infra.NaverLocalSearchClient;
import com.daytodo.domain.place.infra.NaverLocalSearchResponse;
import com.daytodo.domain.place.repository.PlacePriceEstimateRepository;
import com.daytodo.domain.place.repository.PlaceRepository;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.enums.RegionLevel;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseAiRecommendationServiceTest {
    @Mock RegionRepository regionRepository;
    @Mock PlaceRepository placeRepository;
    @Mock PlacePriceEstimateRepository placePriceEstimateRepository;
    @Mock NaverLocalSearchClient naverLocalSearchClient;
    @Mock AiPriceInferenceClient aiPriceInferenceClient;

    private CourseAiRecommendationService service;
    private Region hongdae;

    @BeforeEach
    void setUp() {
        service = new CourseAiRecommendationService(regionRepository, placeRepository,
                placePriceEstimateRepository, naverLocalSearchClient, aiPriceInferenceClient);
        hongdae = new Region(null, "홍대", RegionLevel.SIGUNGU);
        ReflectionTestUtils.setField(hongdae, "regionId", 1L);
        when(regionRepository.findById(1L)).thenReturn(Optional.of(hongdae));
    }

    @Test
    void returnsRestaurantCafeAndActivityCourseWithinBudget() {
        Place restaurant = place(1L, "식당");
        Place cafe = place(2L, "카페");
        Place activity = place(3L, "놀거리");
        when(naverLocalSearchClient.search("홍대 식당")).thenReturn(response("식당", "restaurant-link"));
        when(naverLocalSearchClient.search("홍대 카페")).thenReturn(response("카페", "cafe-link"));
        when(naverLocalSearchClient.search("홍대 놀거리")).thenReturn(response("놀거리", "activity-link"));
        when(placeRepository.findByNaverPlaceId(anyString()))
                .thenReturn(Optional.of(restaurant), Optional.of(cafe), Optional.of(activity));
        when(placePriceEstimateRepository.findByPlace(any())).thenReturn(Optional.empty());
        when(aiPriceInferenceClient.estimate(any())).thenReturn(Map.of(
                "식당-0", new AiPriceInferenceClient.PriceEstimate(12_000, 15_000, .8, "식사 가격"),
                "카페-0", new AiPriceInferenceClient.PriceEstimate(5_000, 7_000, .8, "음료 가격"),
                "놀거리-0", new AiPriceInferenceClient.PriceEstimate(8_000, 9_000, .8, "이용 가격")
        ));
        when(placePriceEstimateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse.AiRecommendations result = service.recommend(
                new CourseRequest.AiRecommendation(1L, 25_000, 35_000));

        assertThat(result.success()).isTrue();
        assertThat(result.code()).isEqualTo("COMMON200");
        assertThat(result.result()).hasSize(1);
        assertThat(result.result().get(0).estimatedTotalMinPrice()).isEqualTo(25_000);
        assertThat(result.result().get(0).estimatedTotalMaxPrice()).isEqualTo(31_000);
        assertThat(result.result().get(0).places()).extracting(CourseResponse.AiRecommendationPlace::recommendationOrder)
                .containsExactly(1, 2, 3);
    }

    @Test
    void returnsEmptyListWhenNoCombinationMatchesBudget() {
        when(naverLocalSearchClient.search(anyString())).thenReturn(new NaverLocalSearchResponse(0, 0, 0, List.of()));
        when(aiPriceInferenceClient.estimate(List.of())).thenReturn(Map.of());

        CourseResponse.AiRecommendations result = service.recommend(
                new CourseRequest.AiRecommendation(1L, 10_000, 30_000));

        assertThat(result.message()).isEqualTo("해당 조건의 장소가 없습니다.");
        assertThat(result.success()).isTrue();
        assertThat(result.code()).isEqualTo("COMMON200");
        assertThat(result.result()).isEmpty();
    }

    private Place place(Long id, String name) {
        Place place = new Place(hongdae, name + "-id", name, name, "서울 마포구", null,
                37.5, 126.9, null, null, null);
        ReflectionTestUtils.setField(place, "placeId", id);
        return place;
    }

    private NaverLocalSearchResponse response(String name, String link) {
        return new NaverLocalSearchResponse(1, 1, 1, List.of(
                new NaverLocalSearchResponse.Item(name, link, name, "설명", null,
                        "서울 마포구", "서울 마포구", "1269000000", "375000000")
        ));
    }
}
