package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.CoursePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, Long> {

    /*
     * 코스 장소 목록 조회 (placeOrder 순)
     * 응답의 placeName, category 가 필요하므로 Place 를 함께 fetch 한다.
     */
    @Query("""
            select cp from CoursePlace cp
            join fetch cp.place p
            where cp.course.courseId = :courseId
            order by cp.placeOrder asc
            """)
    List<CoursePlace> findPlacesByCourseId(@Param("courseId") Long courseId);
}
