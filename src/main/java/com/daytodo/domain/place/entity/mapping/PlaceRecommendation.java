package com.daytodo.domain.place.entity.mapping;

import com.daytodo.domain.common.BaseCreatedEntity;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.enums.PlaceRecommendationSource;
import com.daytodo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_recommendation")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PlaceRecommendation extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "recommendation_id", nullable = false)
    private Long recommendationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // AI 추천은 추천자 없음 → NULL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommender_id")
    private User recommender;

    @Enumerated(EnumType.STRING)
    @Column (name = "source", nullable = false, length = 20)
    private PlaceRecommendationSource source;

    @Column (name = "is_selected", nullable = false)
    private boolean isSelected = false;
}
