package com.daytodo.domain.place.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_price_estimate")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PlacePriceEstimate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "price_estimate_id", nullable = false)
    private Long priceEstimateId;

    // 장소당 추정 1개 → UNIQUE
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false, unique = true)
    private Place place;

    @Min(0) // 최소값 0원
    @Column (name = "min_price", nullable = false)
    private int minPrice;

    // max_price >= min_price
    @Column (name = "max_price", nullable = false)
    private int maxPrice;

    // 추론 신뢰도 (0~1)
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column (name = "confidence", nullable = false)
    private double confidence;

    // 추론 근거
    @Column (name = "reason", length = 500)
    private String reason;
}
