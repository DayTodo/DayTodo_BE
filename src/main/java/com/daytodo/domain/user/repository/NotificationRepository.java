package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.Notification;
import com.daytodo.domain.user.enums.NotificationDeliveryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByUser_IdAndCourseIdAndNotificationType(
            Long userId,
            Long courseId,
            com.daytodo.domain.user.enums.NotificationType notificationType
    );

    @Query("""
            select notification.id from Notification notification
            where notification.deliveryStatus in :statuses
              and notification.attemptCount < :maxAttempts
              and (notification.nextRetryAt is null or notification.nextRetryAt <= :now)
            order by notification.createdAt asc, notification.id asc
            """)
    List<Long> findRetryableIds(
            @Param("statuses") Collection<NotificationDeliveryStatus> statuses,
            @Param("maxAttempts") int maxAttempts,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select notification from Notification notification join fetch notification.user where notification.id = :id")
    Optional<Notification> findByIdForDelivery(@Param("id") Long id);
}
