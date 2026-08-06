package com.daytodo.domain.user.entity;
import com.daytodo.domain.user.enums.NotificationType;
import com.daytodo.domain.user.enums.NotificationDeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(
        name = "notification",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_user_course_type",
                columnNames = {"user_id", "course_id", "notification_type"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "course_id")
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 30, nullable = false)
    private NotificationType notificationType;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "body", length = 500, nullable = false)
    private String body;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", length = 20, nullable = false)
    private NotificationDeliveryStatus deliveryStatus;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Notification(
            User user,
            Long courseId,
            NotificationType notificationType,
            String title,
            String body
    ) {
        this.user = user;
        this.courseId = courseId;
        this.notificationType = notificationType;
        this.title = title;
        this.body = body;
        this.read = false;
        this.deliveryStatus = NotificationDeliveryStatus.PENDING;
        this.attemptCount = 0;
    }

    public void markAsRead() {
        this.read = true;
    }

    public void markSent(LocalDateTime sentAt) {
        this.deliveryStatus = NotificationDeliveryStatus.SENT;
        this.attemptCount++;
        this.sentAt = sentAt;
        this.nextRetryAt = null;
        this.lastError = null;
    }

    public void markFailed(String error, LocalDateTime nextRetryAt) {
        this.deliveryStatus = NotificationDeliveryStatus.FAILED;
        this.attemptCount++;
        this.nextRetryAt = nextRetryAt;
        this.lastError = error == null ? null : error.substring(0, Math.min(error.length(), 1000));
    }
}
