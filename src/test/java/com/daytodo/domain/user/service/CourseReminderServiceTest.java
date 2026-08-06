package com.daytodo.domain.user.service;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberRole;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.enums.ParticipantType;
import com.daytodo.domain.course.repository.CourseMemberRepository;
import com.daytodo.domain.user.entity.FcmToken;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.entity.UserNotificationSetting;
import com.daytodo.domain.user.enums.DevicePlatform;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.NotificationType;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.repository.FcmTokenRepository;
import com.daytodo.domain.user.repository.UserNotificationSettingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseReminderServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 6);

    @Mock CourseMemberRepository courseMemberRepository;
    @Mock UserNotificationSettingRepository settingRepository;
    @Mock FcmTokenRepository fcmTokenRepository;
    @Mock NotificationEventCreator eventCreator;

    @Test
    void createsD1ReminderForUserWithDefaultSettingsAndToken() {
        User user = user();
        Course course = course(user, TODAY.plusDays(1));
        CourseMember member = new CourseMember(course, user, MemberRole.OWNER, MemberStatus.JOINED);
        when(courseMemberRepository.findReminderCandidates(
                eq(TODAY.plusDays(1)), any(Collection.class), eq(MemberStatus.JOINED), eq(UserStatus.ACTIVE)))
                .thenReturn(List.of(member));
        when(courseMemberRepository.findReminderCandidates(
                eq(TODAY), any(Collection.class), eq(MemberStatus.JOINED), eq(UserStatus.ACTIVE)))
                .thenReturn(List.of());
        when(settingRepository.findAllByUser_IdIn(any())).thenReturn(List.of());
        when(fcmTokenRepository.findAllByUser_IdIn(any()))
                .thenReturn(List.of(new FcmToken(user, "token", DevicePlatform.ANDROID)));

        service().createDailyReminders(TODAY);

        verify(eventCreator).create(
                eq(user), eq(10L), eq(NotificationType.COURSE_D1), any(String.class), any(String.class));
    }

    @Test
    void skipsReminderWhenMasterPushIsDisabled() {
        User user = user();
        Course course = course(user, TODAY.plusDays(1));
        CourseMember member = new CourseMember(course, user, MemberRole.OWNER, MemberStatus.JOINED);
        UserNotificationSetting setting = new UserNotificationSetting(user);
        setting.updatePushEnabled(false);
        when(courseMemberRepository.findReminderCandidates(
                eq(TODAY.plusDays(1)), any(Collection.class), eq(MemberStatus.JOINED), eq(UserStatus.ACTIVE)))
                .thenReturn(List.of(member));
        when(courseMemberRepository.findReminderCandidates(
                eq(TODAY), any(Collection.class), eq(MemberStatus.JOINED), eq(UserStatus.ACTIVE)))
                .thenReturn(List.of());
        when(settingRepository.findAllByUser_IdIn(any())).thenReturn(List.of(setting));
        when(fcmTokenRepository.findAllByUser_IdIn(any()))
                .thenReturn(List.of(new FcmToken(user, "token", DevicePlatform.ANDROID)));

        service().createDailyReminders(TODAY);

        verify(eventCreator, never()).create(any(), any(), any(), any(), any());
    }

    private CourseReminderService service() {
        return new CourseReminderService(
                courseMemberRepository, settingRepository, fcmTokenRepository, eventCreator);
    }

    private User user() {
        User user = new User("user@example.com", "password", "user", null, LoginType.LOCAL);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Course course(User user, LocalDate date) {
        Course course = new Course(user, null, "데이트", date, 10000, 20000, ParticipantType.COUPLE);
        ReflectionTestUtils.setField(course, "courseId", 10L);
        ReflectionTestUtils.setField(course, "courseStatus", CourseStatus.PLANNING);
        return course;
    }
}
