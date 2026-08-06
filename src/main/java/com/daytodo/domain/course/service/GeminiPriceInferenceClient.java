package com.daytodo.domain.course.service;

import com.daytodo.domain.course.exception.code.CourseErrorCode;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.Duration;

/** Gemini의 구조화된 출력으로 아직 가격 캐시가 없는 장소들의 1인 가격을 한 번에 추론한다. */
@Slf4j
@Component
public class GeminiPriceInferenceClient implements AiPriceInferenceClient {
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public GeminiPriceInferenceClient(
            ObjectMapper objectMapper,
            @Value("${GEMINI_API_KEY:}") String apiKey,
            @Value("${GEMINI_MODEL:gemini-3.1-flash-lite}") String model
    ) {
        this.objectMapper = objectMapper;
        this.model = model;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder()
                .baseUrl(GEMINI_BASE_URL)
                .defaultHeader("x-goog-api-key", apiKey)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Map<String, PriceEstimate> estimate(List<PlaceInput> places) {
        if (places.isEmpty()) return Map.of();
        try {
            String response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt(places))))),
                            "generationConfig", Map.of(
                                    "responseMimeType", "application/json",
                                    "responseSchema", responseSchema()
                            )
                    ))
                    .retrieve()
                    .body(String.class);
            return parse(response);
        } catch (RestClientException | IllegalArgumentException exception) {
            log.error("Gemini 가격 추론 호출에 실패했습니다.", exception);
            throw new ProjectException(CourseErrorCode.COURSE_RECOMMENDATION_FAILED);
        }
    }

    private String prompt(List<PlaceInput> places) {
        String candidates = places.stream()
                .map(place -> "key=" + place.key() + ", type=" + place.courseType()
                        + ", name=" + place.placeName() + ", category=" + place.category()
                        + ", description=" + nullToEmpty(place.description()))
                .reduce((left, right) -> left + "\n" + right)
                .orElseThrow();
        return "한국의 지역 장소를 기준으로 1인 평균 이용 금액을 원화로 추론하세요. "
                + "가격은 일반적인 메뉴·이용권 기준이어야 하며, reason은 한국어로 작성하세요.\nCandidates:\n" + candidates;
    }

    private Map<String, Object> responseSchema() {
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of("estimates", Map.of(
                        "type", "ARRAY",
                        "items", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                        "key", Map.of("type", "STRING"),
                                        "minPrice", Map.of("type", "INTEGER", "minimum", 0),
                                        "maxPrice", Map.of("type", "INTEGER", "minimum", 0),
                                        "confidence", Map.of("type", "NUMBER", "minimum", 0, "maximum", 1),
                                        "reason", Map.of("type", "STRING")
                                ),
                                "required", List.of("key", "minPrice", "maxPrice", "confidence", "reason")
                        )
                )),
                "required", List.of("estimates")
        );
    }

    private Map<String, PriceEstimate> parse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode estimates = objectMapper.readTree(root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text").asText()).path("estimates");
            Map<String, PriceEstimate> result = new LinkedHashMap<>();
            for (JsonNode estimate : estimates) {
                int minPrice = estimate.path("minPrice").asInt(-1);
                int maxPrice = estimate.path("maxPrice").asInt(-1);
                double confidence = estimate.path("confidence").asDouble(-1);
                String key = estimate.path("key").asText();
                if (!key.isBlank() && minPrice >= 0 && maxPrice >= minPrice && confidence >= 0 && confidence <= 1) {
                    result.put(key, new PriceEstimate(minPrice, maxPrice, confidence, estimate.path("reason").asText(null)));
                }
            }
            if (result.isEmpty()) throw new IllegalArgumentException("Gemini price response is empty");
            return result;
        } catch (Exception exception) {
            log.error("Gemini 가격 추론 응답을 해석하지 못했습니다.", exception);
            throw new ProjectException(CourseErrorCode.COURSE_RECOMMENDATION_FAILED);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
