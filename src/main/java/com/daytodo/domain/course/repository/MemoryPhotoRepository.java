package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.MemoryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemoryPhotoRepository extends JpaRepository<MemoryPhoto, Long> {
  
    List<MemoryPhoto> findAllByDiary_IdOrderByPhotoOrderAsc(Long diaryId);

    List<MemoryPhoto> findAllByDiary_Course_IdAndDiary_User_IdOrderByPhotoOrderAsc(Long courseId, Long userId);

    // 이미 저장된 사진이 있으면 그 뒤 순서부터 부여하기 위해 사용한다. (없으면 0)
    @Query("""
            select coalesce(max(mp.photoOrder), 0)
            from MemoryPhoto mp
            where mp.course.courseId = :courseId
            """)
    int findMaxPhotoOrderByCourseId(@Param("courseId") Long courseId);
}
           