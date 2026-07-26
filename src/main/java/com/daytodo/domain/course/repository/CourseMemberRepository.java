package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseMemberRepository extends JpaRepository<CourseMember, Long> {

    List<CourseMember> findByCourseIdAndMemberStatus(Long courseId, MemberStatus memberStatus);

    boolean existsByCourseIdAndUserIdAndMemberStatus(Long courseId, Long userId, MemberStatus memberStatus);

    Optional<CourseMember> findByCourseIdAndUserIdAndMemberStatus(
            Long courseId, Long userId, MemberStatus memberStatus);
}
