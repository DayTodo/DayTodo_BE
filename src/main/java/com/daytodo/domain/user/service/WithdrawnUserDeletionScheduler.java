package com.daytodo.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawnUserDeletionScheduler {
    private final WithdrawnUserDeletionService deletionService;

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteExpiredWithdrawnUsers() {
        int deletedCount = deletionService.deleteExpiredWithdrawnUsers();
        log.info("Permanently deleted {} users past the withdrawal retention period", deletedCount);
    }
}
