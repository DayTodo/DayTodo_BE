package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /*
     * 투데이 코스 조회
     * 사용자가 참여 중인 코스 중 해당 날짜의 진행 중 코스를 조회한다.
     * 이론상 1건이지만 데이터 이상으로 여러 건일 수 있어 최근 생성 순으로 정렬해 첫 건을 사용한다.
     */
    @Query("""
            select cm.course from CourseMember cm
            where cm.user.id = :userId
              and cm.memberStatus = :memberStatus
              and cm.course.courseDate = :courseDate
              and cm.course.courseStatus = :courseStatus
            order by cm.course.createdAt desc
            """)
    List<Course> findMemberCoursesByDateAndStatus(
            @Param("userId") Long userId,
            @Param("courseDate") LocalDate courseDate,
            @Param("courseStatus") CourseStatus courseStatus,
            @Param("memberStatus") MemberStatus memberStatus
    );
}
