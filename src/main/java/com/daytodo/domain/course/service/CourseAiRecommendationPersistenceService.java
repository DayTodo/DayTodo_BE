package com.daytodo.domain.course.service;

import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.entity.PlacePriceEstimate;
import com.daytodo.domain.place.repository.PlacePriceEstimateRepository;
import com.daytodo.domain.place.repository.PlaceRepository;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.exception.code.RegionErrorCode;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
class CourseAiRecommendationPersistenceService {
    private final RegionRepository regionRepository;
    private final PlaceRepository placeRepository;
    private final PlacePriceEstimateRepository placePriceEstimateRepository;

    @Transactional
    public List<AiCourseCandidate> resolveCandidates(Long regionId, List<AiCourseCandidate> candidates) {
        if (candidates.isEmpty()) return List.of();
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new ProjectException(RegionErrorCode.REGION_NOT_FOUND));
        return candidates.stream().map(candidate -> {
            Place place = placeRepository.findByNaverPlaceId(candidate.externalId())
                    .orElseGet(() -> placeRepository.save(new Place(region, candidate.externalId(), candidate.placeName(), candidate.category(),
                            candidate.address(), candidate.roadAddress(), candidate.latitude(), candidate.longitude(), candidate.phone(),
                            candidate.description(), null)));
            return candidate.withPlace(place, placePriceEstimateRepository.findByPlace(place));
        }).toList();
    }

    @Transactional
    public List<AiCourseCandidate> savePriceEstimates(List<AiCourseCandidate> candidates,
                                                        Map<String, AiPriceInferenceClient.PriceEstimate> inferred) {
        Map<Long, AiCourseCandidate> uniqueMissingCandidates = new LinkedHashMap<>();
        for (AiCourseCandidate candidate : candidates) {
            if (candidate.priceEstimate().isEmpty()) uniqueMissingCandidates.putIfAbsent(candidate.place().getPlaceId(), candidate);
        }

        Map<Long, PlacePriceEstimate> savedEstimates = new LinkedHashMap<>();
        for (AiCourseCandidate candidate : uniqueMissingCandidates.values()) {
            PlacePriceEstimate estimate = placePriceEstimateRepository.findByPlace(candidate.place())
                    .orElseGet(() -> saveEstimate(candidate, inferred.get(candidate.key())));
            if (estimate != null) savedEstimates.put(candidate.place().getPlaceId(), estimate);
        }

        return candidates.stream()
                .map(candidate -> candidate.priceEstimate().map(candidate::withPriceEstimate)
                        .orElseGet(() -> {
                            PlacePriceEstimate estimate = savedEstimates.get(candidate.place().getPlaceId());
                            return estimate == null ? null : candidate.withPriceEstimate(estimate);
                        }))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private PlacePriceEstimate saveEstimate(AiCourseCandidate candidate, AiPriceInferenceClient.PriceEstimate estimate) {
        if (estimate == null) return null;
        return placePriceEstimateRepository.save(new PlacePriceEstimate(candidate.place(), estimate.minPrice(), estimate.maxPrice(),
                estimate.confidence(), estimate.reason()));
    }
}
