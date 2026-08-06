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
import com.daytodo.domain.region.exception.code.RegionErrorCode;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseAiRecommendationService {
    private static final String NO_PLACES_MESSAGE = "해당 조건의 장소가 없습니다.";
    private static final String SUCCESS_MESSAGE = "성공적으로 요청을 처리했습니다.";
    private static final String SUCCESS_CODE = "COMMON200";
    private static final List<String> COURSE_TYPES = List.of("식당", "카페", "놀거리");

    private final RegionRepository regionRepository;
    private final PlaceRepository placeRepository;
    private final PlacePriceEstimateRepository placePriceEstimateRepository;
    private final NaverLocalSearchClient naverLocalSearchClient;
    private final AiPriceInferenceClient aiPriceInferenceClient;

    @Transactional
    public CourseResponse.AiRecommendations recommend(CourseRequest.AiRecommendation request) {
        Region region = regionRepository.findById(request.regionId())
                .orElseThrow(() -> new ProjectException(RegionErrorCode.REGION_NOT_FOUND));

        List<Candidate> candidates = searchCandidates(region);
        List<Candidate> pricedCandidates = saveMissingPriceEstimates(candidates);
        List<CourseResponse.AiRecommendationCourse> courses = combine(
                region.getRegionName(), pricedCandidates, request.minPrice(), request.maxPrice());
        return new CourseResponse.AiRecommendations(true, SUCCESS_CODE,
                courses.isEmpty() ? NO_PLACES_MESSAGE : SUCCESS_MESSAGE, courses);
    }

    private List<Candidate> searchCandidates(Region region) {
        List<Candidate> candidates = new ArrayList<>();
        for (String type : COURSE_TYPES) {
            NaverLocalSearchResponse response = naverLocalSearchClient.search(region.getRegionName() + " " + type);
            if (response == null || response.items() == null) continue;
            IntStream.range(0, response.items().size())
                    .mapToObj(index -> toCandidate(region, type, index, response.items().get(index)))
                    .forEach(candidates::add);
        }
        return candidates;
    }

    private Candidate toCandidate(Region region, String type, int index, NaverLocalSearchResponse.Item item) {
        String externalId = externalId(item);
        Place place = placeRepository.findByNaverPlaceId(externalId)
                .orElseGet(() -> placeRepository.save(new Place(region, externalId, clean(item.title()), category(item.category()),
                        requiredText(item.address()), emptyToNull(item.roadAddress()), coordinate(item.mapy()), coordinate(item.mapx()),
                        emptyToNull(item.telephone()), emptyToNull(clean(item.description())), null)));
        return new Candidate(type + "-" + index, type, place, item, placePriceEstimateRepository.findByPlace(place));
    }

    private List<Candidate> saveMissingPriceEstimates(List<Candidate> candidates) {
        List<AiPriceInferenceClient.PlaceInput> inputs = candidates.stream()
                .filter(candidate -> candidate.priceEstimate().isEmpty())
                .map(candidate -> new AiPriceInferenceClient.PlaceInput(candidate.key(), candidate.type(), candidate.place().getPlaceName(),
                        candidate.place().getCategory(), candidate.item().description()))
                .toList();
        Map<String, AiPriceInferenceClient.PriceEstimate> inferred = aiPriceInferenceClient.estimate(inputs);
        return candidates.stream()
                .map(candidate -> candidate.priceEstimate().<Candidate>map(price -> candidate)
                        .orElseGet(() -> saveEstimatedCandidate(candidate, inferred.get(candidate.key()))))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private Candidate saveEstimatedCandidate(Candidate candidate, AiPriceInferenceClient.PriceEstimate estimate) {
        if (estimate == null) return null;
        PlacePriceEstimate saved = placePriceEstimateRepository.save(new PlacePriceEstimate(candidate.place(), estimate.minPrice(),
                estimate.maxPrice(), estimate.confidence(), estimate.reason()));
        return candidate.withPriceEstimate(saved);
    }

    private List<CourseResponse.AiRecommendationCourse> combine(
            String regionName, List<Candidate> candidates, int minBudget, int maxBudget) {
        List<Candidate> restaurants = byType(candidates, "식당");
        List<Candidate> cafes = byType(candidates, "카페");
        List<Candidate> activities = byType(candidates, "놀거리");
        List<List<Candidate>> valid = new ArrayList<>();
        for (Candidate restaurant : restaurants) for (Candidate cafe : cafes) for (Candidate activity : activities) {
            List<Candidate> course = List.of(restaurant, cafe, activity);
            int totalMin = course.stream().mapToInt(candidate -> candidate.priceEstimate().orElseThrow().getMinPrice()).sum();
            int totalMax = course.stream().mapToInt(candidate -> candidate.priceEstimate().orElseThrow().getMaxPrice()).sum();
            if (totalMin >= minBudget && totalMax <= maxBudget) valid.add(course);
        }
        List<List<Candidate>> selected = valid.stream()
                .sorted(Comparator.comparingInt(course -> course.stream().mapToInt(c -> c.priceEstimate().orElseThrow().getMaxPrice()).sum()))
                .limit(2).toList();
        return IntStream.range(0, selected.size())
                .mapToObj(index -> toCourse(regionName, index + 1, selected.get(index)))
                .toList();
    }

    private List<Candidate> byType(List<Candidate> candidates, String type) {
        return candidates.stream().filter(candidate -> candidate.type().equals(type)).toList();
    }

    private CourseResponse.AiRecommendationCourse toCourse(String regionName, int courseNumber, List<Candidate> candidates) {
        int totalMin = candidates.stream().mapToInt(candidate -> candidate.priceEstimate().orElseThrow().getMinPrice()).sum();
        int totalMax = candidates.stream().mapToInt(candidate -> candidate.priceEstimate().orElseThrow().getMaxPrice()).sum();
        List<CourseResponse.AiRecommendationPlace> places = IntStream.range(0, candidates.size())
                .mapToObj(index -> toPlace(index + 1, candidates.get(index))).toList();
        return new CourseResponse.AiRecommendationCourse(regionName + " AI 추천 코스 " + courseNumber, totalMin, totalMax, places);
    }

    private CourseResponse.AiRecommendationPlace toPlace(int order, Candidate candidate) {
        Place place = candidate.place();
        PlacePriceEstimate estimate = candidate.priceEstimate().orElseThrow();
        return new CourseResponse.AiRecommendationPlace(order, place.getPlaceId(), place.getNaverPlaceId(), place.getPlaceName(),
                place.getCategory(), place.getAddress(), place.getRoadAddress(), place.getLatitude(), place.getLongitude(),
                place.getDescription(), place.getImageUrl(), estimate.getMinPrice(), estimate.getMaxPrice(), estimate.getConfidence(), estimate.getReason());
    }

    private String externalId(NaverLocalSearchResponse.Item item) {
        if (item.link() != null && !item.link().isBlank()) return item.link();
        return sha256(clean(item.title()) + "|" + requiredText(item.address()) + "|" + emptyToNull(item.roadAddress()));
    }
    private String sha256(String value) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
    private String clean(String value) { return value == null ? "" : value.replaceAll("<[^>]*>", ""); }
    private String category(String value) { return value == null || value.isBlank() ? "기타" : value.replace(">", " > "); }
    private String requiredText(String value) { return value == null ? "" : value; }
    private String emptyToNull(String value) { return value == null || value.isBlank() ? null : value; }
    private double coordinate(String value) {
        try { return value == null || value.isBlank() ? 0 : Double.parseDouble(value) / 10_000_000; }
        catch (NumberFormatException exception) { return 0; }
    }

    private record Candidate(String key, String type, Place place, NaverLocalSearchResponse.Item item,
                             Optional<PlacePriceEstimate> priceEstimate) {
        private Candidate withPriceEstimate(PlacePriceEstimate estimate) {
            return new Candidate(key, type, place, item, Optional.of(estimate));
        }
    }
}
