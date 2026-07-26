package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.MemoryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoryPhotoRepository extends JpaRepository<MemoryPhoto, Long> {

    List<MemoryPhoto> findAllByDiary_IdOrderByPhotoOrderAsc(Long diaryId);

    List<MemoryPhoto> findAllByDiary_Course_IdAndDiary_User_IdOrderByPhotoOrderAsc(Long courseId, Long userId);
}