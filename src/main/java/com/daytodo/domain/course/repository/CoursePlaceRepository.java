package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.CoursePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, Long> {

    @Query("""
        SELECT cp FROM CoursePlace cp
        WHERE cp.courseId = :courseId
        ORDER BY cp.placeOrder ASC
    """)
    List<CoursePlace> findByCourseIdOrderByPlaceOrderAsc(@Param("courseId") Long courseId);
}