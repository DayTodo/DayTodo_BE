package com.daytodo.domain.user.service;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.repository.CourseMemberRepository;
import com.daytodo.domain.user.entity.FcmToken;
import com.daytodo.domain.user.entity.UserNotificationSetting;
import com.daytodo.domain.user.enums.NotificationType;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.repository.FcmTokenRepository;
import com.daytodo.domain.user.repository.UserNotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseReminderService {

    private static final Set<CourseStatus> EXCLUDED_STATUSES =
            EnumSet.of(CourseStatus.CANCELED, CourseStatus.COMPLETED);

    private final CourseMemberRepository courseMemberRepository;
    private final UserNotificationSettingRepository settingRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationEventCreator eventCreator;

    public void createDailyReminders(LocalDate today) {
        createReminders(today.plusDays(1), NotificationType.COURSE_D1);
        createReminders(today, NotificationType.COURSE_D0);
    }

    private void createReminders(LocalDate courseDate, NotificationType type) {
        var candidates = courseMemberRepository.findReminderCandidates(
                courseDate,
                EXCLUDED_STATUSES,
                MemberStatus.JOINED,
                UserStatus.ACTIVE
        );
        if (candidates.isEmpty()) {
            return;
        }

        Set<Long> userIds = candidates.stream()
                .map(candidate -> candidate.getUser().getId())
                .collect(Collectors.toSet());
        Map<Long, UserNotificationSetting> settings = settingRepository.findAllByUser_IdIn(userIds).stream()
                .collect(Collectors.toMap(setting -> setting.getUser().getId(), Function.identity()));
        Set<Long> usersWithTokens = fcmTokenRepository.findAllByUser_IdIn(userIds).stream()
                .map(FcmToken::getUser)
                .map(user -> user.getId())
                .collect(Collectors.toSet());

        candidates.stream()
                .filter(candidate -> usersWithTokens.contains(candidate.getUser().getId()))
                .filter(candidate -> isEnabled(settings.get(candidate.getUser().getId())))
                .forEach(candidate -> createReminder(candidate, type));
    }

    private boolean isEnabled(UserNotificationSetting setting) {
        return setting == null || setting.isPushEnabled();
    }

    private void createReminder(CourseMember candidate, NotificationType type) {
        Course course = candidate.getCourse();
        String title = type == NotificationType.COURSE_D1
                ? "코스 일정이 하루 남았어요"
                : "오늘 예정된 코스가 있어요";
        String body = type == NotificationType.COURSE_D1
                ? String.format("내일 '%s' 코스가 예정되어 있어요.", course.getCourseName())
                : String.format("오늘 '%s' 코스를 확인해 보세요.", course.getCourseName());
        try {
            eventCreator.create(candidate.getUser(), course.getCourseId(), type, title, body);
        } catch (DataIntegrityViolationException exception) {
            log.debug("이미 생성된 코스 알림입니다. userId={}, courseId={}, type={}",
                    candidate.getUser().getId(), course.getCourseId(), type);
        }
    }
}
