package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CoursePlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<CoursePlace> findByCourseIdOrderByPlaceOrderAsc(Long courseId);
}