package com.daytodo.domain.user.service;

import com.daytodo.domain.user.entity.FcmToken;
import com.daytodo.domain.user.entity.Notification;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.DevicePlatform;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.NotificationDeliveryStatus;
import com.daytodo.domain.user.enums.NotificationType;
import com.daytodo.domain.user.push.PushNotificationGateway;
import com.daytodo.domain.user.repository.FcmTokenRepository;
import com.daytodo.domain.user.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryWorkerTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-06T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock NotificationRepository notificationRepository;
    @Mock FcmTokenRepository fcmTokenRepository;
    @Mock PushNotificationGateway pushGateway;

    @Test
    void marksNotificationSentWhenAtLeastOneDeviceSucceeds() {
        Notification notification = notification();
        FcmToken token = new FcmToken(notification.getUser(), "token", DevicePlatform.ANDROID);
        when(notificationRepository.findByIdForDelivery(100L)).thenReturn(Optional.of(notification));
        when(fcmTokenRepository.findAllByUser_Id(1L)).thenReturn(List.of(token));
        when(pushGateway.send(eq("token"), any(), any(), any()))
                .thenReturn(PushNotificationGateway.PushResult.sent());

        worker().deliver(100L);

        assertThat(notification.getDeliveryStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
        assertThat(notification.getAttemptCount()).isEqualTo(1);
        assertThat(notification.getSentAt()).isNotNull();
    }

    @Test
    void removesInvalidTokenAndSchedulesRetry() {
        Notification notification = notification();
        FcmToken token = new FcmToken(notification.getUser(), "invalid", DevicePlatform.IOS);
        when(notificationRepository.findByIdForDelivery(100L)).thenReturn(Optional.of(notification));
        when(fcmTokenRepository.findAllByUser_Id(1L)).thenReturn(List.of(token));
        when(pushGateway.send(eq("invalid"), any(), any(), any()))
                .thenReturn(PushNotificationGateway.PushResult.failed(true, "unregistered"));

        worker().deliver(100L);

        verify(fcmTokenRepository).delete(token);
        assertThat(notification.getDeliveryStatus()).isEqualTo(NotificationDeliveryStatus.FAILED);
        assertThat(notification.getNextRetryAt()).isNotNull();
    }

    private NotificationDeliveryWorker worker() {
        return new NotificationDeliveryWorker(
                notificationRepository, fcmTokenRepository, pushGateway, CLOCK);
    }

    private Notification notification() {
        User user = new User("user@example.com", "password", "user", null, LoginType.LOCAL);
        ReflectionTestUtils.setField(user, "id", 1L);
        Notification notification = new Notification(
                user, 10L, NotificationType.COURSE_D0, "title", "body");
        ReflectionTestUtils.setField(notification, "id", 100L);
        return notification;
    }
}
