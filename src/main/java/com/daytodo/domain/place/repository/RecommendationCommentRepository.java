package com.daytodo.domain.place.repository;

import com.daytodo.domain.place.entity.RecommendationComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationCommentRepository extends JpaRepository<RecommendationComment, Long> {

    // 1. 특정 추천 장소에 달린 삭제되지 않은 댓글 개수 조회 (리스트 조회 시 사용)
    int countByRecommendation_RecommendationIdAndIsDeletedFalse(Long recommendationId);

    // (참고) 만약 단순 전체 개수가 필요하다면 아래 메서드도 활용 가능
    // int countByRecommendation_RecommendationId(Long recommendationId);
}