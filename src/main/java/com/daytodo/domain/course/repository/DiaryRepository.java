package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    Optional<Diary> findByUserIdAndCourseId(Long userId, Long courseId);

    Optional<Diary> findByUserIdAndDiaryDate(Long userId, LocalDate diaryDate);

    List<Diary> findAllByUserIdAndDiaryDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}