package com.daytodo.domain.user.service;

import com.daytodo.domain.user.entity.FcmToken;
import com.daytodo.domain.user.entity.Notification;
import com.daytodo.domain.user.enums.NotificationDeliveryStatus;
import com.daytodo.domain.user.push.PushNotificationGateway;
import com.daytodo.domain.user.repository.FcmTokenRepository;
import com.daytodo.domain.user.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryWorker {

    private static final int MAX_ATTEMPTS = 5;

    private final NotificationRepository notificationRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final PushNotificationGateway pushGateway;
    private final Clock clock;

    @Transactional
    public void deliver(Long notificationId) {
        Notification notification = notificationRepository.findByIdForDelivery(notificationId)
                .orElse(null);
        if (!isRetryable(notification)) {
            return;
        }

        var tokens = fcmTokenRepository.findAllByUser_Id(notification.getUser().getId());
        if (tokens.isEmpty()) {
            notification.markFailed("등록된 FCM 토큰이 없습니다.", nextRetryAt(notification));
            return;
        }

        boolean sent = false;
        var errors = new ArrayList<String>();
        for (FcmToken token : tokens) {
            PushNotificationGateway.PushResult result = pushGateway.send(
                    token.getToken(),
                    notification.getTitle(),
                    notification.getBody(),
                    Map.of(
                            "courseId", String.valueOf(notification.getCourseId()),
                            "notificationType", notification.getNotificationType().name()
                    )
            );
            if (result.success()) {
                sent = true;
            } else {
                if (result.invalidToken()) {
                    fcmTokenRepository.delete(token);
                }
                errors.add(result.errorMessage() == null ? "FCM 발송 실패" : result.errorMessage());
            }
        }

        if (sent) {
            notification.markSent(LocalDateTime.now(clock));
        } else {
            notification.markFailed(String.join(" | ", errors), nextRetryAt(notification));
        }
    }

    private boolean isRetryable(Notification notification) {
        if (notification == null || notification.getAttemptCount() >= MAX_ATTEMPTS) {
            return false;
        }
        if (notification.getDeliveryStatus() != NotificationDeliveryStatus.PENDING
                && notification.getDeliveryStatus() != NotificationDeliveryStatus.FAILED) {
            return false;
        }
        return notification.getNextRetryAt() == null
                || !notification.getNextRetryAt().isAfter(LocalDateTime.now(clock));
    }

    private LocalDateTime nextRetryAt(Notification notification) {
        long delayMinutes = 1L << Math.min(notification.getAttemptCount(), 5);
        return LocalDateTime.now(clock).plusMinutes(delayMinutes);
    }
}
