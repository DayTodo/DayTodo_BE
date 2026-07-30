package com.daytodo.domain.place.repository;

import com.daytodo.domain.place.entity.mapping.RecommendationLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationLikeRepository extends JpaRepository<RecommendationLike, Long> {

    // 1. 특정 추천에 대해 유저가 이미 좋아요를 눌렀는지 확인 (1인 1회 제한)
    boolean existsByRecommendation_RecommendationIdAndUser_Id(Long recommendationId, Long userId);

    // 2. 특정 추천의 총 좋아요 수 조회
    int countByRecommendation_RecommendationId(Long recommendationId);
}