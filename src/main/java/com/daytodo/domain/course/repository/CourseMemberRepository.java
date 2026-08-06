package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;


public interface CourseMemberRepository extends JpaRepository<CourseMember, Long> {
    Optional<CourseMember> findByCourseCourseIdAndUserId(Long courseId, Long userId);

    boolean existsByCourseCourseIdAndUserIdAndMemberStatus(Long courseId, Long userId, MemberStatus memberStatus);

    List<CourseMember> findByCourseCourseIdAndMemberStatus(Long courseId, MemberStatus memberStatus);

    @Query("""
            select cm from CourseMember cm
            join fetch cm.course course
            join fetch cm.user user
            where course.courseDate = :courseDate
              and course.courseStatus not in :excludedStatuses
              and cm.memberStatus = :memberStatus
              and user.userStatus = :userStatus
            order by course.courseId asc, user.id asc
            """)
    List<CourseMember> findReminderCandidates(
            @Param("courseDate") LocalDate courseDate,
            @Param("excludedStatuses") Collection<CourseStatus> excludedStatuses,
            @Param("memberStatus") MemberStatus memberStatus,
            @Param("userStatus") UserStatus userStatus
    );

    Optional<CourseMember> findByCourseCourseIdAndUserIdAndMemberStatus(Long courseId, Long targetUserId, MemberStatus memberStatus);

    /*
     * 코스 멤버 목록 조회
     * 응답의 nickname, profileImageUrl 이 필요하므로 User 를 함께 fetch 한다.
     */
    @Query("""
            select cm from CourseMember cm
            join fetch cm.user u
            where cm.course.courseId = :courseId
              and cm.memberStatus = :memberStatus
            order by cm.joinedAt asc, cm.courseMemberId asc
            """)
    List<CourseMember> findMembersByCourseId(
            @Param("courseId") Long courseId,
            @Param("memberStatus") MemberStatus memberStatus
    );
           
    interface CourseCount {
        Long getCourseId();
        long getCount();
    }

    @Query("""
            select cm.course.courseId as courseId, count(cm) as count
            from CourseMember cm
            where cm.course.courseId in :courseIds and cm.memberStatus = :status
            group by cm.course.courseId
            """)
    List<CourseCount> countMembersByCourseIds(
            @Param("courseIds") Collection<Long> courseIds,
            @Param("status") MemberStatus status
    );
}
