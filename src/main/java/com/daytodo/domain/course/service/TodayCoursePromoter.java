package com.daytodo.domain.course.service;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 코스 상태 지연 전환 헬퍼.
 * 별도 스케줄러가 없으므로, courseDate 가 도래한 PLANNING 코스를
 * 홈/투데이 조회 진입 시점에 IN_PROGRESS 로 승격시킨다.
 * 두 조회 경로가 같은 규칙을 쓰도록 승격 로직을 한곳에 모은다.
 */
@Component
@RequiredArgsConstructor
public class TodayCoursePromoter {

    private final CourseRepository courseRepository;

    /**
     * 사용자가 참여 중인 코스 중 courseDate 가 today 인 PLANNING 코스를 IN_PROGRESS 로 승격.
     * 호출 측 트랜잭션에 합류하며(REQUIRED), 변경 감지로 커밋 시 반영된다.
     */
    @Transactional
    public void promoteDueCourses(Long userId, LocalDate today) {
        courseRepository.findMemberCoursesByDateAndStatuses(
                userId,
                today,
                List.of(CourseStatus.PLANNING),
                MemberStatus.JOINED
        ).forEach(Course::start);
    }
}
