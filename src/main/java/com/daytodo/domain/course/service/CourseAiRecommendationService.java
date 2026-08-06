package com.daytodo.domain.course.service;

import com.daytodo.domain.course.dto.CourseRequest;
import com.daytodo.domain.course.dto.CourseResponse;
import com.daytodo.domain.place.infra.NaverLocalSearchClient;
import com.daytodo.domain.place.infra.NaverLocalSearchResponse;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.exception.code.RegionErrorCode;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

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
public class CourseAiRecommendationService {
    private static final String NO_PLACES_MESSAGE = "해당 조건의 장소가 없습니다.";
    private static final String SUCCESS_MESSAGE = "성공적으로 요청을 처리했습니다.";
    private static final String SUCCESS_CODE = "COMMON200";
    private static final List<String> COURSE_TYPES = List.of("식당", "카페", "놀거리");

    private final RegionRepository regionRepository;
    private final CourseAiRecommendationPersistenceService persistenceService;
    private final NaverLocalSearchClient naverLocalSearchClient;
    private final AiPriceInferenceClient aiPriceInferenceClient;

    public CourseResponse.AiRecommendations recommend(CourseRequest.AiRecommendation request) {
        Region region = regionRepository.findById(request.regionId())
                .orElseThrow(() -> new ProjectException(RegionErrorCode.REGION_NOT_FOUND));

        // Naver/Gemini 원격 호출은 트랜잭션 밖에서 수행한다.
        List<AiCourseCandidate> discoveredCandidates = searchCandidates(region.getRegionName());
        List<AiCourseCandidate> candidates = persistenceService.resolveCandidates(region.getRegionId(), discoveredCandidates);
        if (candidates.isEmpty()) return emptyResponse();
        Map<String, AiPriceInferenceClient.PriceEstimate> inferred = aiPriceInferenceClient.estimate(toPriceInputs(candidates));
        List<AiCourseCandidate> pricedCandidates = persistenceService.savePriceEstimates(candidates, inferred);

        List<CourseResponse.AiRecommendationCourse> courses = combine(
                region.getRegionName(), pricedCandidates, request.minPrice(), request.maxPrice());
        return courses.isEmpty() ? emptyResponse() : new CourseResponse.AiRecommendations(true, SUCCESS_CODE, SUCCESS_MESSAGE, courses);
    }

    private CourseResponse.AiRecommendations emptyResponse() {
        return new CourseResponse.AiRecommendations(true, SUCCESS_CODE, NO_PLACES_MESSAGE, List.of());
    }

    private List<AiCourseCandidate> searchCandidates(String regionName) {
        List<AiCourseCandidate> candidates = new ArrayList<>();
        for (String type : COURSE_TYPES) {
            NaverLocalSearchResponse response = naverLocalSearchClient.search(regionName + " " + type);
            if (response == null || response.items() == null) continue;
            IntStream.range(0, response.items().size())
                    .mapToObj(index -> toCandidate(type, index, response.items().get(index)))
                    .flatMap(Optional::stream)
                    .forEach(candidates::add);
        }
        return candidates;
    }

    private Optional<AiCourseCandidate> toCandidate(String type, int index, NaverLocalSearchResponse.Item item) {
        Double latitude = coordinate(item.mapy());
        Double longitude = coordinate(item.mapx());
        if (latitude == null || longitude == null) return Optional.empty();
        return Optional.of(AiCourseCandidate.discovered(type + "-" + index, type, externalId(item), clean(item.title()),
                category(item.category()), requiredText(item.address()), emptyToNull(item.roadAddress()), latitude, longitude,
                emptyToNull(item.telephone()), emptyToNull(clean(item.description()))));
    }

    private List<AiPriceInferenceClient.PlaceInput> toPriceInputs(List<AiCourseCandidate> candidates) {
        return candidates.stream()
                .filter(candidate -> candidate.priceEstimate().isEmpty())
                .collect(java.util.stream.Collectors.toMap(candidate -> candidate.place().getPlaceId(), candidate -> candidate,
                        (left, right) -> left, java.util.LinkedHashMap::new))
                .values().stream()
                .map(candidate -> new AiPriceInferenceClient.PlaceInput(candidate.key(), candidate.type(), candidate.place().getPlaceName(),
                        candidate.place().getCategory(), candidate.description()))
                .toList();
    }

    private List<CourseResponse.AiRecommendationCourse> combine(
            String regionName, List<AiCourseCandidate> candidates, int minBudget, int maxBudget) {
        List<AiCourseCandidate> restaurants = byType(candidates, "식당");
        List<AiCourseCandidate> cafes = byType(candidates, "카페");
        List<AiCourseCandidate> activities = byType(candidates, "놀거리");
        List<List<AiCourseCandidate>> valid = new ArrayList<>();
        for (AiCourseCandidate restaurant : restaurants) for (AiCourseCandidate cafe : cafes) for (AiCourseCandidate activity : activities) {
            List<AiCourseCandidate> course = List.of(restaurant, cafe, activity);
            int totalMin = course.stream().mapToInt(candidate -> candidate.priceEstimate().orElseThrow().getMinPrice()).sum();
            int totalMax = course.stream().mapToInt(candidate -> candidate.priceEstimate().orElseThrow().getMaxPrice()).sum();
            if (totalMin >= minBudget && totalMax <= maxBudget) valid.add(course);
        }
        List<List<AiCourseCandidate>> selected = valid.stream()
                .sorted(Comparator.comparingInt(course -> course.stream().mapToInt(c -> c.priceEstimate().orElseThrow().getMaxPrice()).sum()))
                .limit(2).toList();
        return IntStream.range(0, selected.size())
                .mapToObj(index -> toCourse(regionName, index + 1, selected.get(index)))
                .toList();
    }

    private List<AiCourseCandidate> byType(List<AiCourseCandidate> candidates, String type) {
        return candidates.stream().filter(candidate -> candidate.type().equals(type)).toList();
    }

    private CourseResponse.AiRecommendationCourse toCourse(String regionName, int courseNumber, List<AiCourseCandidate> candidates) {
        int totalMin = candidates.stream().mapToInt(candidate -> candidate.priceEstimate().orElseThrow().getMinPrice()).sum();
        int totalMax = candidates.stream().mapToInt(candidate -> candidate.priceEstimate().orElseThrow().getMaxPrice()).sum();
        List<CourseResponse.AiRecommendationPlace> places = IntStream.range(0, candidates.size())
                .mapToObj(index -> toPlace(index + 1, candidates.get(index))).toList();
        return new CourseResponse.AiRecommendationCourse(regionName + " AI 추천 코스 " + courseNumber, totalMin, totalMax, places);
    }

    private CourseResponse.AiRecommendationPlace toPlace(int order, AiCourseCandidate candidate) {
        var place = candidate.place();
        var estimate = candidate.priceEstimate().orElseThrow();
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
    private String clean(String value) { return value == null ? "" : HtmlUtils.htmlUnescape(value.replaceAll("<[^>]*>", "")); }
    private String category(String value) { return value == null || value.isBlank() ? "기타" : clean(value).replace(">", " > "); }
    private String requiredText(String value) { return value == null ? "" : value; }
    private String emptyToNull(String value) { return value == null || value.isBlank() ? null : value; }
    private Double coordinate(String value) {
        try { return value == null || value.isBlank() ? null : Double.parseDouble(value) / 10_000_000; }
        catch (NumberFormatException exception) { return null; }
    }
}
