package com.daytodo.domain.place.repository;

import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.entity.PlacePriceEstimate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlacePriceEstimateRepository extends JpaRepository<PlacePriceEstimate, Long> {
    Optional<PlacePriceEstimate> findByPlace(Place place);
}
