package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CourseQueryValidationTest {

    @Autowired
    CourseRepository courseRepository;
    @Autowired
    CourseMemberRepository courseMemberRepository;
    @Autowired
    CoursePlaceRepository coursePlaceRepository;

    @Test
    @DisplayName("투데이 코스 조회 JPQL이 정상적으로 실행된다")
    void queriesRun() {
        assertThat(courseRepository.findMemberCoursesByDateAndStatus(
                1L, LocalDate.now(), CourseStatus.IN_PROGRESS, MemberStatus.JOINED
        )).isEmpty();

        assertThat(courseMemberRepository.findMembersByCourseId(1L, MemberStatus.JOINED)).isEmpty();
        assertThat(coursePlaceRepository.findPlacesByCourseId(1L)).isEmpty();
    }
}
