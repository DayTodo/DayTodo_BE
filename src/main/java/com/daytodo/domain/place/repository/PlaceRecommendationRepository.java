package com.daytodo.domain.place.repository;

import com.daytodo.domain.place.entity.mapping.PlaceRecommendation; // 엔티티 패키지 경로에 맞게 수정해주세요
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRecommendationRepository extends JpaRepository<PlaceRecommendation, Long> {

    // 1. 코스에 해당 장소가 이미 추천되었는지 중복 확인 (장소 추천 API에서 사용)
    boolean existsByCourse_CourseIdAndPlace_PlaceId(Long courseId, Long placeId);

    // 2. 특정 코스의 전체 추천 장소 리스트 조회
    List<PlaceRecommendation> findByCourse_CourseId(Long courseId);

    // 3. 특정 코스의 특정 출처(예: "ai") 추천 장소 리스트 조회
    List<PlaceRecommendation> findByCourse_CourseIdAndSource(Long courseId, String source);

    // 4. 특정 코스에서 특정 출처를 제외한(예: "ai" 제외 -> 멤버 추천) 추천 장소 리스트 조회
    List<PlaceRecommendation> findByCourse_CourseIdAndSourceNot(Long courseId, String source);
}