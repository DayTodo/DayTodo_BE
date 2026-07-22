package com.daytodo.domain.user.service;

import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.domain.user.repository.WithdrawnUserCleanupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawnUserDeletionService {
    private static final long RETENTION_DAYS = 30;

    private final UserRepository userRepository;
    private final WithdrawnUserCleanupRepository cleanupRepository;
    private final Clock clock;

    @Transactional
    public int deleteExpiredWithdrawnUsers() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusDays(RETENTION_DAYS);
        List<User> expiredUsers = userRepository.findAllByUserStatusAndWithdrawnAtLessThanEqual(
                UserStatus.WITHDRAWN,
                cutoff
        );
        expiredUsers.forEach(user -> {
            cleanupRepository.removeUserReferences(user.getId());
            userRepository.delete(user);
        });
        return expiredUsers.size();
    }
}
