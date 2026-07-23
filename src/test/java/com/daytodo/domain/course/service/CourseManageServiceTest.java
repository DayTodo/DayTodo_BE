package com.daytodo.domain.course.service;

import com.daytodo.domain.course.dto.CourseReqDto;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.entity.CoursePlace;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberRole;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.enums.ParticipantType;
import com.daytodo.domain.course.exception.code.CourseErrorCode;
import com.daytodo.domain.course.repository.CourseMemberRepository;
import com.daytodo.domain.course.repository.CoursePlaceRepository;
import com.daytodo.domain.course.repository.CourseRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseManageServiceTest {

    private static final Long COURSE_ID = 1L;
    private static final Long OWNER_ID = 10L;
    private static final Long MEMBER_USER_ID = 20L;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CoursePlaceRepository coursePlaceRepository;

    @Mock
    private CourseMemberRepository courseMemberRepository;

    @InjectMocks
    private CourseManageService courseManageService;

    private Course course;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setCourseId(COURSE_ID);
        course.setOwnerId(OWNER_ID);
        course.setCourseName("기존 코스");
        course.setRegionId(1L);
        course.setCourseDate(LocalDate.of(2026, 8, 1));
        course.setMinPrice(10_000);
        course.setMaxPrice(30_000);
        course.setParticipantType(ParticipantType.COUPLE);
        course.setCourseStatus(CourseStatus.PLANNING);
    }

    @Test
    void 최소_가격이_최대_가격보다_크면_코스를_수정할_수_없다() {
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        ProjectException exception = assertThrows(ProjectException.class,
                () -> courseManageService.updateCourseSetting(
                        COURSE_ID, OWNER_ID, settingRequest(30_000, 10_000, course.getCourseDate())
                ));

        assertEquals(CourseErrorCode.INVALID_PRICE_RANGE, exception.getErrorCode());
    }

    @Test
    void 진행_중인_코스의_날짜는_변경할_수_없다() {
        course.setCourseStatus(CourseStatus.IN_PROGRESS);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        ProjectException exception = assertThrows(ProjectException.class,
                () -> courseManageService.updateCourseSetting(
                        COURSE_ID, OWNER_ID, settingRequest(10_000, 30_000, LocalDate.of(2026, 8, 2))
                ));

        assertEquals(CourseErrorCode.COURSE_DATE_CHANGE_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    void 참여_멤버는_장소를_순서대로_조회할_수_있다() {
        CoursePlace firstPlace = coursePlace(101L, 1);
        CoursePlace secondPlace = coursePlace(102L, 2);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseMemberRepository.existsByCourseIdAndUserIdAndMemberStatus(
                COURSE_ID, MEMBER_USER_ID, MemberStatus.JOINED)).thenReturn(true);
        when(coursePlaceRepository.findByCourseIdOrderByPlaceOrderAsc(COURSE_ID))
                .thenReturn(List.of(firstPlace, secondPlace));

        var response = courseManageService.getCoursePlaces(COURSE_ID, MEMBER_USER_ID);

        assertEquals(List.of(101L, 102L), response.stream().map(place -> place.placeId()).toList());
        assertEquals(List.of(1, 2), response.stream().map(place -> place.placeOrder()).toList());
    }

    @Test
    void 방장은_참여_멤버를_강퇴할_수_있다() {
        CourseMember owner = courseMember(OWNER_ID, MemberRole.OWNER);
        CourseMember target = courseMember(MEMBER_USER_ID, MemberRole.MEMBER);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseMemberRepository.findByCourseIdAndUserIdAndMemberStatus(
                COURSE_ID, OWNER_ID, MemberStatus.JOINED)).thenReturn(Optional.of(owner));
        when(courseMemberRepository.findByCourseIdAndUserIdAndMemberStatus(
                COURSE_ID, MEMBER_USER_ID, MemberStatus.JOINED)).thenReturn(Optional.of(target));

        courseManageService.kickCourseMember(COURSE_ID, MEMBER_USER_ID, OWNER_ID);

        assertEquals(MemberStatus.LEFT, target.getMemberStatus());
    }

    private CourseReqDto.SettingReq settingRequest(Integer minPrice, Integer maxPrice, LocalDate courseDate) {
        return new CourseReqDto.SettingReq(
                "변경 코스", 2L, courseDate, minPrice, maxPrice, ParticipantType.COUPLE
        );
    }

    private CoursePlace coursePlace(Long placeId, Integer placeOrder) {
        CoursePlace coursePlace = new CoursePlace();
        coursePlace.setPlaceId(placeId);
        coursePlace.setPlaceOrder(placeOrder);
        return coursePlace;
    }

    private CourseMember courseMember(Long userId, MemberRole memberRole) {
        CourseMember courseMember = new CourseMember();
        courseMember.setCourseId(COURSE_ID);
        courseMember.setUserId(userId);
        courseMember.setMemberRole(memberRole);
        courseMember.setMemberStatus(MemberStatus.JOINED);
        return courseMember;
    }
}
