package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /*
     * 추억 사진 저장처럼 코스 단위로 순번을 계산하는 임계 구역을 직렬화하기 위해
     * 코스 행에 쓰기 락(PESSIMISTIC_WRITE)을 건다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Course c where c.courseId = :courseId")
    Optional<Course> findByIdForUpdate(@Param("courseId") Long courseId);

    List<Course> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Course> findAllByOwnerIdAndCourseDateGreaterThanEqualOrderByCreatedAtDesc(Long ownerId, LocalDate startDate);

    List<Course> findAllByOwnerIdAndCourseDateLessThanEqualOrderByCreatedAtDesc(Long ownerId, LocalDate endDate);

    List<Course> findAllByOwnerIdAndCourseDateBetweenOrderByCreatedAtDesc(
            Long ownerId,
            LocalDate startDate,
            LocalDate endDate
    );

    /*
     * 투데이 코스 조회
     * 사용자가 참여 중인 코스 중 해당 날짜의 코스를 조회한다.
     * PLANNING 은 조회 시점에 IN_PROGRESS 로 승격되므로 두 상태를 모두 대상으로 한다.
     * 이론상 1건이지만 데이터 이상으로 여러 건일 수 있어 최근 생성 순으로 정렬해 첫 건을 사용한다.
     */
    @Query("""
            select cm.course from CourseMember cm
            where cm.user.id = :userId
              and cm.memberStatus = :memberStatus
              and cm.course.courseDate = :courseDate
              and cm.course.courseStatus in :courseStatuses
            order by cm.course.createdAt desc, cm.course.courseId desc
            """)
    List<Course> findMemberCoursesByDateAndStatuses(
            @Param("userId") Long userId,
            @Param("courseDate") LocalDate courseDate,
            @Param("courseStatuses") Collection<CourseStatus> courseStatuses,
            @Param("memberStatus") MemberStatus memberStatus
    );

    @Query("""
            select cm.course from CourseMember cm
            where cm.user.id = :userId
              and cm.memberStatus = :memberStatus
              and cm.course.courseStatus = :courseStatus
            order by cm.course.courseDate desc, cm.course.courseId desc
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
            order by cm.course.createdAt desc, cm.course.courseId desc
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
            order by cm.course.courseDate desc, cm.course.courseId desc
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
            order by cm.course.courseDate asc, cm.course.createdAt desc, cm.course.courseId desc
            """)
    List<Course> findCalendarCourses(
            @Param("userId") Long userId,
            @Param("memberStatus") MemberStatus memberStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
