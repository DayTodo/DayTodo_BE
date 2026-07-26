package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.CoursePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, Long> {

    List<CoursePlace> findAllByCourseIdOrderByPlaceOrderAsc(Long courseId);

    List<CoursePlace> findByCourseIdOrderByPlaceOrderAsc(Long courseId);

    interface CourseCount {
        Long getCourseId();
        long getCount();
    }

    @Query("""
            select cp.course.courseId as courseId, count(cp) as count
            from CoursePlace cp
            where cp.course.courseId in :courseIds
            group by cp.course.courseId
            """)
    List<CourseCount> countPlacesByCourseIds(@Param("courseIds") Collection<Long> courseIds);
}