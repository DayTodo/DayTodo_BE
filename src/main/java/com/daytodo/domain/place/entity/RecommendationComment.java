package com.daytodo.domain.place.entity;

import com.daytodo.domain.common.BaseEntity;
import com.daytodo.domain.place.entity.mapping.PlaceRecommendation;
import com.daytodo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommendation_comment")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RecommendationComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "comment_id", nullable = false)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private PlaceRecommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column (name = "content", nullable = false, length = 500)
    private String content;

    // 소프트 삭제
    @Column (name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
