package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseMemberRepository extends JpaRepository<CourseMember, Long> {

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
}
