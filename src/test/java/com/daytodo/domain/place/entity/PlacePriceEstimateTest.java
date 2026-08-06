package com.daytodo.domain.place.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlacePriceEstimateTest {

    @Test
    void rejectsPriceRangeWithMaxPriceLowerThanMinPrice() {
        assertThatThrownBy(() -> new PlacePriceEstimate(null, 10_000, 5_000, 0.8, "가격 추론"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 가격은 최소 가격보다 작을 수 없습니다.");
    }
}
