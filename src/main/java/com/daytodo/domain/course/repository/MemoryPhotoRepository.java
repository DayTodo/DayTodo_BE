package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.MemoryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemoryPhotoRepository extends JpaRepository<MemoryPhoto, Long> {

    // 추억 사진은 코스 멤버 전원이 함께 보는 공용 사진(피그마 '기록' 화면에서 여러 멤버가
    // 같은 사진에 메모를 남기는 것으로 확인)이라, diary와 무관하게 course 기준으로 조회한다.
    List<MemoryPhoto> findAllByCourse_CourseIdOrderByPhotoOrderAsc(Long courseId);

    // 이미 저장된 사진이 있으면 그 뒤 순서부터 부여하기 위해 사용한다. (없으면 0)
    @Query("""
            select coalesce(max(mp.photoOrder), 0)
            from MemoryPhoto mp
            where mp.course.courseId = :courseId
            """)
    int findMaxPhotoOrderByCourseId(@Param("courseId") Long courseId);
}