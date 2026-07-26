package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseMemberRepository extends JpaRepository<CourseMember, Long> {
    Optional<CourseMember> findByCourseCourseIdAndUserId(Long courseId, Long userId);

    Optional<Object> findByCourseIdAndUserIdAndMemberStatus(Long courseId, Long userId, MemberStatus memberStatus);

    List<CourseMember> findByCourseIdAndMemberStatus(Long courseId, MemberStatus memberStatus);

    Optional<CourseMember> findByCourse_CourseIdAndUser_UserIdAndMemberStatus(Long courseId, Long userId, MemberStatus status);

    boolean existsByCourseIdAndUserIdAndMemberStatus(Long courseId, Long userId, MemberStatus memberStatus);

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
