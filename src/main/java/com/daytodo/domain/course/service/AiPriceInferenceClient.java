package com.daytodo.domain.course.service;

import java.util.List;
import java.util.Map;

/** OpenAI 등 가격 추론 제공자를 교체 가능하게 분리한 포트. */
public interface AiPriceInferenceClient {
    Map<String, PriceEstimate> estimate(List<PlaceInput> places);

    record PlaceInput(String key, String courseType, String placeName, String category, String description) {
    }

    record PriceEstimate(int minPrice, int maxPrice, double confidence, String reason) {
    }
}
