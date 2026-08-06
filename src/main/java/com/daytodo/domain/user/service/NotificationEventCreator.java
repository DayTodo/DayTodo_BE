package com.daytodo.domain.user.service;

import com.daytodo.domain.user.entity.Notification;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.NotificationType;
import com.daytodo.domain.user.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationEventCreator {

    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(
            User user,
            Long courseId,
            NotificationType type,
            String title,
            String body
    ) {
        if (notificationRepository.existsByUser_IdAndCourseIdAndNotificationType(
                user.getId(), courseId, type)) {
            return;
        }
        notificationRepository.saveAndFlush(new Notification(user, courseId, type, title, body));
    }
}
