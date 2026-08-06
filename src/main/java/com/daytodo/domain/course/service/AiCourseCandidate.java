package com.daytodo.domain.course.service;

import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.entity.PlacePriceEstimate;

import java.util.Optional;

record AiCourseCandidate(
        String key, String type, String externalId, String placeName, String category, String address, String roadAddress,
        double latitude, double longitude, String phone, String description, Place place, Optional<PlacePriceEstimate> priceEstimate
) {
    static AiCourseCandidate discovered(String key, String type, String externalId, String placeName, String category, String address,
                                       String roadAddress, double latitude, double longitude, String phone, String description) {
        return new AiCourseCandidate(key, type, externalId, placeName, category, address, roadAddress, latitude, longitude, phone, description,
                null, Optional.empty());
    }

    AiCourseCandidate withPlace(Place place, Optional<PlacePriceEstimate> priceEstimate) {
        return new AiCourseCandidate(key, type, externalId, placeName, category, address, roadAddress, latitude, longitude, phone, description,
                place, priceEstimate);
    }

    AiCourseCandidate withPriceEstimate(PlacePriceEstimate estimate) {
        return new AiCourseCandidate(key, type, externalId, placeName, category, address, roadAddress, latitude, longitude, phone, description,
                place, Optional.of(estimate));
    }
}
