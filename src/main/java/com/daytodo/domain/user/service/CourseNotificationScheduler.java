package com.daytodo.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CourseNotificationScheduler {

    private final CourseReminderService reminderService;
    private final NotificationDispatchService dispatchService;
    private final Clock clock;

    @Scheduled(
            cron = "${notification.course-reminder-cron:0 0 9 * * *}",
            zone = "${notification.zone:Asia/Seoul}"
    )
    public void createDailyReminders() {
        reminderService.createDailyReminders(LocalDate.now(clock));
        dispatchService.dispatchRetryable();
    }

    @Scheduled(fixedDelayString = "${notification.dispatch-delay-ms:60000}")
    public void retryFailedNotifications() {
        dispatchService.dispatchRetryable();
    }
}
