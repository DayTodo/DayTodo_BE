package com.daytodo.domain.place.entity.mapping;

import com.daytodo.domain.common.BaseCreatedEntity;
import com.daytodo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "recommendation_like",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recommendation_like_recommendation_user",
                columnNames = {"recommendation_id", "user_id"}
        )
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RecommendationLike extends BaseCreatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "recommendation_like_id", nullable = false)
    private Long recommendationLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private PlaceRecommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
