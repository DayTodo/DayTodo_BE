package com.daytodo.domain.course.service;

import com.daytodo.domain.course.dto.CourseRequest;
import com.daytodo.domain.course.dto.CourseResponse;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.entity.InviteCode;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.HomeBannerStatus;
import com.daytodo.domain.course.enums.MemberRole;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.enums.ParticipantType;
import com.daytodo.domain.course.exception.code.CourseErrorCode;
import com.daytodo.domain.course.repository.CourseMemberRepository;
import com.daytodo.domain.course.repository.CoursePlaceRepository;
import com.daytodo.domain.course.repository.CourseRepository;
import com.daytodo.domain.course.repository.InviteCodeRepository;
import com.daytodo.domain.place.repository.PlaceRecommendationRepository;
import com.daytodo.domain.place.repository.PlaceRepository;
import com.daytodo.domain.place.repository.RecommendationCommentRepository;
import com.daytodo.domain.place.repository.RecommendationLikeRepository;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.enums.RegionLevel;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.service.UserService;
import com.daytodo.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 22);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-22T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock CourseRepository courseRepository;
    @Mock CourseMemberRepository courseMemberRepository;
    @Mock CoursePlaceRepository coursePlaceRepository;
    @Mock InviteCodeRepository inviteCodeRepository;
    @Mock RegionRepository regionRepository;
    @Mock UserService userService;
    @Mock PlaceRecommendationRepository placeRecommendationRepository;
    @Mock RecommendationLikeRepository recommendationLikeRepository;
    @Mock PlaceRepository placeRepository;
    @Mock RecommendationCommentRepository recommendationCommentRepository;
    @Mock TodayCoursePromoter todayCoursePromoter;

    CourseService courseService;
    User user;
    Region gangnam;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(
                courseRepository,
                courseMemberRepository,
                coursePlaceRepository,
                inviteCodeRepository,
                regionRepository,
                userService,
                placeRecommendationRepository,
                recommendationLikeRepository,
                placeRepository,
                recommendationCommentRepository,
                todayCoursePromoter,
                CLOCK
        );
        user = new User("user@example.com", "password", "user", null, LoginType.LOCAL);
        ReflectionTestUtils.setField(user, "id", 1L);
        Region seoul = region(10L, null, "서울특별시", RegionLevel.SIDO);
        gangnam = region(11L, seoul, "강남구", RegionLevel.SIGUNGU);
        when(userService.getActiveUser(1L)).thenReturn(user);
    }

    @Test
    void inProgressBannerHasPriority() {
        Course running = course(100L, "진행 코스", TODAY, CourseStatus.IN_PROGRESS);
        Course soon = course(101L, "예정 코스", TODAY.plusDays(2), CourseStatus.PLANNING);
        when(courseRepository.findMemberCoursesByStatus(1L, MemberStatus.JOINED, CourseStatus.IN_PROGRESS))
                .thenReturn(List.of(running));
        when(courseRepository.findUpcomingMemberCourses(
                eq(1L), eq(MemberStatus.JOINED), eq(CourseStatus.PLANNING), eq(TODAY), any(Pageable.class)
        )).thenReturn(List.of(soon));
        when(courseRepository.findAllByOwnerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        CourseResponse.Courses response = courseService.getCourses(1L, null, null);

        assertThat(response.banner().status()).isEqualTo(HomeBannerStatus.IN_PROGRESS);
        assertThat(response.banner().message()).isEqualTo("오늘 진행 코스 일정이 있어요!");
        verify(courseRepository, never()).findApproachingMemberCourses(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    void showsUpcomingBannerForCourseWithinThreeDays() {
        Course soon = course(101L, "D3 코스", TODAY.plusDays(3), CourseStatus.PLANNING);
        stubEmptyHome();
        when(courseRepository.findApproachingMemberCourses(
                1L, MemberStatus.JOINED, CourseStatus.PLANNING, TODAY, TODAY.plusDays(3)
        )).thenReturn(List.of(soon));

        CourseResponse.Courses response = courseService.getCourses(1L, null, null);

        assertThat(response.banner().status()).isEqualTo(HomeBannerStatus.UPCOMING);
        assertThat(response.banner().courseId()).isEqualTo(101L);
    }

    @Test
    void returnsEmptyHomeCollectionsAndBannerWhenNoScheduleExists() {
        stubEmptyHome();
        when(courseRepository.findApproachingMemberCourses(
                1L, MemberStatus.JOINED, CourseStatus.PLANNING, TODAY, TODAY.plusDays(3)
        )).thenReturn(List.of());

        CourseResponse.Courses response = courseService.getCourses(1L, null, null);

        assertThat(response.banner().status()).isEqualTo(HomeBannerStatus.EMPTY);
        assertThat(response.inProgressCourses()).isEmpty();
        assertThat(response.upcomingCourses()).isEmpty();
        assertThat(response.createdCourses()).isEmpty();
    }

    @Test
    void calculatesUpcomingCourseDDay() {
        Course upcoming = course(101L, "D2 코스", TODAY.plusDays(2), CourseStatus.PLANNING);
        when(courseRepository.findMemberCoursesByStatus(1L, MemberStatus.JOINED, CourseStatus.IN_PROGRESS))
                .thenReturn(List.of());
        when(courseRepository.findUpcomingMemberCourses(
                eq(1L), eq(MemberStatus.JOINED), eq(CourseStatus.PLANNING), eq(TODAY), any(Pageable.class)
        )).thenReturn(List.of(upcoming));
        when(courseRepository.findAllByOwnerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(courseRepository.findApproachingMemberCourses(
                1L, MemberStatus.JOINED, CourseStatus.PLANNING, TODAY, TODAY.plusDays(3)
        )).thenReturn(List.of(upcoming));

        CourseResponse.Courses response = courseService.getCourses(1L, null, null);

        assertThat(response.upcomingCourses()).hasSize(1);
        assertThat(response.upcomingCourses().get(0).dDay()).isEqualTo(2);
    }

    @Test
    void rejectsInvalidCreatedCoursePeriod() {
        assertThatThrownBy(() -> courseService.getCourses(
                1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 7, 1)
        )).isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_COURSE_PERIOD);
    }

    @Test
    void appliesDateFilterOnlyToCreatedCourses() {
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);
        when(courseRepository.findMemberCoursesByStatus(1L, MemberStatus.JOINED, CourseStatus.IN_PROGRESS))
                .thenReturn(List.of());
        when(courseRepository.findUpcomingMemberCourses(
                eq(1L), eq(MemberStatus.JOINED), eq(CourseStatus.PLANNING), eq(TODAY), any(Pageable.class)
        )).thenReturn(List.of());
        when(courseRepository.findAllByOwnerIdAndCourseDateBetweenOrderByCreatedAtDesc(1L, start, end))
                .thenReturn(List.of());
        when(courseRepository.findApproachingMemberCourses(
                1L, MemberStatus.JOINED, CourseStatus.PLANNING, TODAY, TODAY.plusDays(3)
        )).thenReturn(List.of());

        courseService.getCourses(1L, start, end);

        verify(courseRepository).findAllByOwnerIdAndCourseDateBetweenOrderByCreatedAtDesc(1L, start, end);
    }

    @Test
    void groupsMultipleCalendarCoursesOnSameDate() {
        Course first = course(100L, "첫 코스", TODAY, CourseStatus.PLANNING);
        Course second = course(101L, "둘째 코스", TODAY, CourseStatus.PLANNING);
        when(courseRepository.findCalendarCourses(
                1L,
                MemberStatus.JOINED,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        )).thenReturn(List.of(first, second));

        CourseResponse.Calendar response = courseService.getCalendar(1L, 2026, 7);

        assertThat(response.schedules()).hasSize(1);
        assertThat(response.schedules().get(0).courses()).hasSize(2);
    }

    @Test
    void rejectsInvalidCalendarMonth() {
        assertThatThrownBy(() -> courseService.getCalendar(1L, 2026, 13))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_CALENDAR_DATE);
    }

    @Test
    void returnsEmptyCalendarForMonthWithoutSchedules() {
        when(courseRepository.findCalendarCourses(
                1L,
                MemberStatus.JOINED,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        )).thenReturn(List.of());

        CourseResponse.Calendar response = courseService.getCalendar(1L, 2026, 2);

        assertThat(response.schedules()).isEmpty();
    }

    @Test
    void createsCourseOwnerMembershipAndInviteCodeAtomically() {
        when(regionRepository.findById(11L)).thenReturn(Optional.of(gangnam));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setCourseId(100L);
            return course;
        });
        when(inviteCodeRepository.existsByCode(any())).thenReturn(false);
        CourseRequest.Create request = new CourseRequest.Create(
                "서울 나들이",
                11L,
                TODAY.plusDays(1),
                0,
                50_000,
                ParticipantType.FRIEND
        );

        CourseResponse.Created response = courseService.createCourse(1L, request);

        assertThat(response.courseId()).isEqualTo(100L);
        assertThat(response.inviteCode()).hasSize(12);
        assertThat(response.inviteCodeExpiredAt())
                .isEqualTo(LocalDateTime.of(TODAY.plusDays(1), java.time.LocalTime.MAX));
        verify(courseMemberRepository).save(any(CourseMember.class));
        verify(inviteCodeRepository).save(any(InviteCode.class));
    }

    @Test
    void rejectsPastCourseDate() {
        CourseRequest.Create request = new CourseRequest.Create(
                "과거 코스", 11L, TODAY.minusDays(1), 0, 1000, ParticipantType.ALONE
        );

        assertThatThrownBy(() -> courseService.createCourse(1L, request))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.PAST_COURSE_DATE);
    }

    @Test
    void rejectsCourseOutsideSeoul() {
        Region busan = region(20L, null, "부산광역시", RegionLevel.SIDO);
        Region haeundae = region(21L, busan, "해운대구", RegionLevel.SIGUNGU);
        when(regionRepository.findById(21L)).thenReturn(Optional.of(haeundae));
        CourseRequest.Create request = new CourseRequest.Create(
                "부산 코스", 21L, TODAY.plusDays(1), 0, 1000, ParticipantType.FRIEND
        );

        assertThatThrownBy(() -> courseService.createCourse(1L, request))
                .isInstanceOf(ProjectException.class);
    }

    @Test
    void joinsCourseWithValidInviteCode() {
        Course course = course(100L, "초대 코스", TODAY.plusDays(1), CourseStatus.PLANNING);
        InviteCode inviteCode = new InviteCode(
                course, user, "ABC123", LocalDateTime.of(2026, 7, 23, 23, 59)
        );
        when(inviteCodeRepository.findByCode("ABC123")).thenReturn(Optional.of(inviteCode));
        when(courseMemberRepository.findByCourseCourseIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        CourseResponse.Joined response = courseService.joinCourse(
                1L, new CourseRequest.Join(" ABC123 ")
        );

        assertThat(response.courseId()).isEqualTo(100L);
        verify(courseMemberRepository).saveAndFlush(any(CourseMember.class));
    }

    @Test
    void rejectsDuplicateCourseMember() {
        Course course = course(100L, "중복 코스", TODAY.plusDays(1), CourseStatus.PLANNING);
        InviteCode inviteCode = new InviteCode(
                course, user, "ABC123", LocalDateTime.of(2026, 7, 23, 23, 59)
        );
        CourseMember member = new CourseMember(course, user, MemberRole.MEMBER, MemberStatus.JOINED);
        when(inviteCodeRepository.findByCode("ABC123")).thenReturn(Optional.of(inviteCode));
        when(courseMemberRepository.findByCourseCourseIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(member));

        assertThatThrownBy(() -> courseService.joinCourse(1L, new CourseRequest.Join("ABC123")))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.ALREADY_COURSE_MEMBER);
    }

    @Test
    void rejectsUnknownInviteCode() {
        when(inviteCodeRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.joinCourse(1L, new CourseRequest.Join("UNKNOWN")))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_INVITE_CODE);
    }

    @Test
    void rejectsExpiredInviteCode() {
        Course course = course(100L, "만료 코스", TODAY, CourseStatus.PLANNING);
        InviteCode inviteCode = new InviteCode(
                course, user, "EXPIRED", LocalDateTime.of(2026, 7, 22, 9, 59)
        );
        when(inviteCodeRepository.findByCode("EXPIRED")).thenReturn(Optional.of(inviteCode));

        assertThatThrownBy(() -> courseService.joinCourse(1L, new CourseRequest.Join("EXPIRED")))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.EXPIRED_INVITE_CODE);
    }

    @Test
    void convertsConcurrentDuplicateJoinToBusinessError() {
        Course course = course(100L, "동시 가입", TODAY.plusDays(1), CourseStatus.PLANNING);
        InviteCode inviteCode = new InviteCode(
                course, user, "RACE", LocalDateTime.of(2026, 7, 23, 23, 59)
        );
        when(inviteCodeRepository.findByCode("RACE")).thenReturn(Optional.of(inviteCode));
        when(courseMemberRepository.findByCourseCourseIdAndUserId(100L, 1L)).thenReturn(Optional.empty());
        when(courseMemberRepository.saveAndFlush(any(CourseMember.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> courseService.joinCourse(1L, new CourseRequest.Join("RACE")))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.ALREADY_COURSE_MEMBER);
    }

    private void stubEmptyHome() {
        when(courseRepository.findMemberCoursesByStatus(1L, MemberStatus.JOINED, CourseStatus.IN_PROGRESS))
                .thenReturn(List.of());
        when(courseRepository.findUpcomingMemberCourses(
                eq(1L), eq(MemberStatus.JOINED), eq(CourseStatus.PLANNING), eq(TODAY), any(Pageable.class)
        )).thenReturn(List.of());
        when(courseRepository.findAllByOwnerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
    }

    private Course course(Long id, String name, LocalDate date, CourseStatus status) {
        Course course = new Course(user, gangnam, name, date, 0, 10_000, ParticipantType.FRIEND);
        course.setCourseId(id);
        course.setCourseStatus(status);
        return course;
    }

    private Region region(Long id, Region parent, String name, RegionLevel level) {
        Region region = new Region(parent, name, level);
        ReflectionTestUtils.setField(region, "regionId", id);
        return region;
    }
}
