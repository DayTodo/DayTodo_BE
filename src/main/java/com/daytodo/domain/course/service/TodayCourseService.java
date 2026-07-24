package com.daytodo.domain.course.service;

import com.daytodo.domain.course.converter.TodayCourseConverter;
import com.daytodo.domain.course.dto.response.CourseResDTO;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.entity.CoursePlace;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.repository.CourseMemberRepository;
import com.daytodo.domain.course.repository.CoursePlaceRepository;
import com.daytodo.domain.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TodayCourseService {

    private final CourseRepository courseRepository;
    private final CourseMemberRepository courseMemberRepository;
    private final CoursePlaceRepository coursePlaceRepository;

    /*
     * 투데이 코스 조회
     * 오늘 진행 중인 코스가 없으면 예외가 아니라 todayCourse: null 로 응답한다.
     */
    @Transactional(readOnly = true)
    public CourseResDTO.GetTodayCourse getTodayCourse(Long userId) {
        Optional<Course> todayCourse = courseRepository.findMemberCoursesByDateAndStatus(
                userId,
                LocalDate.now(),
                CourseStatus.IN_PROGRESS,
                MemberStatus.JOINED
        ).stream().findFirst();

        if (todayCourse.isEmpty()) {
            return TodayCourseConverter.toTodayCourse(null, List.of(), List.of());
        }

        Course course = todayCourse.get();
        List<CourseMember> members =
                courseMemberRepository.findMembersByCourseId(course.getCourseId(), MemberStatus.JOINED);
        List<CoursePlace> places =
                coursePlaceRepository.findPlacesByCourseId(course.getCourseId());

        return TodayCourseConverter.toTodayCourse(course, members, places);
    }
}
