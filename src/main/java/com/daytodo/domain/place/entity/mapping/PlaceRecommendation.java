package com.daytodo.domain.place.entity.mapping;

import com.daytodo.domain.common.BaseCreatedEntity;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.enums.PlaceRecommendationSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_recommendation")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PlaceRecommendation extends BaseCreatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "recommendation_id", nullable = false)
    private Long recommendationId;

    // course 도메인 참조 → FK 값만 보관
    @Column (name = "course_id", nullable = false)
    private Long courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // user 도메인 참조 → FK 값만 보관 (AI 추천은 추천자 없음 → NULL)
    @Column (name = "recommender_id")
    private Long recommenderId;

    @Enumerated(EnumType.STRING)
    @Column (name = "source", nullable = false, length = 20)
    private PlaceRecommendationSource source;

    @Column (name = "is_selected", nullable = false)
    private boolean isSelected = false;
}
