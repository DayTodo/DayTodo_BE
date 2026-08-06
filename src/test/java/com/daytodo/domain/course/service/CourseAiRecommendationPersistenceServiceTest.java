package com.daytodo.domain.course.service;

import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.entity.PlacePriceEstimate;
import com.daytodo.domain.place.repository.PlacePriceEstimateRepository;
import com.daytodo.domain.place.repository.PlaceRepository;
import com.daytodo.domain.region.repository.RegionRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseAiRecommendationPersistenceServiceTest {
    @Mock RegionRepository regionRepository;
    @Mock PlaceRepository placeRepository;
    @Mock PlacePriceEstimateRepository placePriceEstimateRepository;

    @Test
    void savesOnlyOneEstimateWhenCandidatesReferToSamePlace() {
        CourseAiRecommendationPersistenceService service = new CourseAiRecommendationPersistenceService(
                regionRepository, placeRepository, placePriceEstimateRepository);
        Place place = new Place(null, "place-key", "중복 장소", "카페", "서울", null, 37.5, 126.9, null, null, null);
        ReflectionTestUtils.setField(place, "placeId", 1L);
        AiCourseCandidate first = candidate("카페-0", place);
        AiCourseCandidate second = candidate("놀거리-0", place);

        when(placePriceEstimateRepository.findByPlace(place)).thenReturn(Optional.empty());
        when(placePriceEstimateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<AiCourseCandidate> result = service.savePriceEstimates(List.of(first, second), Map.of(
                "카페-0", new AiPriceInferenceClient.PriceEstimate(5_000, 8_000, .8, "가격"),
                "놀거리-0", new AiPriceInferenceClient.PriceEstimate(7_000, 10_000, .8, "가격")
        ));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).priceEstimate()).isPresent();
        assertThat(result.get(1).priceEstimate()).isPresent();
        verify(placePriceEstimateRepository, times(1)).save(any(PlacePriceEstimate.class));
    }

    private AiCourseCandidate candidate(String key, Place place) {
        return AiCourseCandidate.discovered(key, "카페", "place-key", "중복 장소", "카페", "서울", null,
                37.5, 126.9, null, null).withPlace(place, Optional.empty());
    }
}
