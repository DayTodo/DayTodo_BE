package com.daytodo.domain.user.service;

import com.daytodo.domain.user.enums.NotificationDeliveryStatus;
import com.daytodo.domain.user.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationDispatchService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BATCH_SIZE = 100;

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryWorker deliveryWorker;
    private final Clock clock;

    public void dispatchRetryable() {
        notificationRepository.findRetryableIds(
                        EnumSet.of(NotificationDeliveryStatus.PENDING, NotificationDeliveryStatus.FAILED),
                        MAX_ATTEMPTS,
                        LocalDateTime.now(clock),
                        PageRequest.of(0, BATCH_SIZE)
                )
                .forEach(deliveryWorker::deliver);
    }
}
