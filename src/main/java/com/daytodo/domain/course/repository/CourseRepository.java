package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Course> findAllByOwnerIdAndCourseDateGreaterThanEqualOrderByCreatedAtDesc(Long ownerId, LocalDate startDate);

    List<Course> findAllByOwnerIdAndCourseDateLessThanEqualOrderByCreatedAtDesc(Long ownerId, LocalDate endDate);

    List<Course> findAllByOwnerIdAndCourseDateBetweenOrderByCreatedAtDesc(
            Long ownerId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
            select cm.course from CourseMember cm
            where cm.user.id = :userId
              and cm.memberStatus = :memberStatus
              and cm.course.courseStatus = :courseStatus
            order by cm.course.courseDate desc
            """)
    List<Course> findMemberCoursesByStatus(
            @Param("userId") Long userId,
            @Param("memberStatus") MemberStatus memberStatus,
            @Param("courseStatus") CourseStatus courseStatus
    );

    @Query("""
            select cm.course from CourseMember cm
            where cm.user.id = :userId
              and cm.memberStatus = :memberStatus
              and cm.course.courseStatus = :courseStatus
              and cm.course.courseDate >= :today
            order by cm.course.createdAt desc
            """)
    List<Course> findUpcomingMemberCourses(
            @Param("userId") Long userId,
            @Param("memberStatus") MemberStatus memberStatus,
            @Param("courseStatus") CourseStatus courseStatus,
            @Param("today") LocalDate today,
            Pageable pageable
    );

    @Query("""
            select cm.course from CourseMember cm
            where cm.user.id = :userId
              and cm.memberStatus = :memberStatus
              and cm.course.courseStatus = :courseStatus
              and cm.course.courseDate between :startDate and :endDate
            order by cm.course.courseDate desc
            """)
    List<Course> findApproachingMemberCourses(
            @Param("userId") Long userId,
            @Param("memberStatus") MemberStatus memberStatus,
            @Param("courseStatus") CourseStatus courseStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select distinct cm.course from CourseMember cm
            where cm.user.id = :userId
              and cm.memberStatus = :memberStatus
              and cm.course.courseDate between :startDate and :endDate
            order by cm.course.courseDate asc, cm.course.createdAt desc
            """)
    List<Course> findCalendarCourses(
            @Param("userId") Long userId,
            @Param("memberStatus") MemberStatus memberStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}