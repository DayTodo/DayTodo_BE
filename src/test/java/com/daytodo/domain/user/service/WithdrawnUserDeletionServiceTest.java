package com.daytodo.domain.user.service;

import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.domain.user.repository.WithdrawnUserCleanupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawnUserDeletionServiceTest {
    @Mock UserRepository userRepository;
    @Mock WithdrawnUserCleanupRepository cleanupRepository;

    @Test
    void deletesUsersWithdrawnForThirtyDaysAfterCleaningReferences() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-22T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        User user = new User("old@example.com", null, "old", null, LoginType.NAVER);
        ReflectionTestUtils.setField(user, "id", 7L);
        user.withdraw(LocalDateTime.of(2026, 6, 22, 10, 0));
        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        when(userRepository.findAllByUserStatusAndWithdrawnAtLessThanEqual(
                org.mockito.ArgumentMatchers.eq(UserStatus.WITHDRAWN),
                cutoff.capture()
        )).thenReturn(List.of(user));
        WithdrawnUserDeletionService service = new WithdrawnUserDeletionService(
                userRepository, cleanupRepository, clock
        );

        int deleted = service.deleteExpiredWithdrawnUsers();

        assertThat(deleted).isOne();
        assertThat(cutoff.getValue()).isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 0));
        verify(cleanupRepository).removeUserReferences(7L);
        verify(userRepository).delete(user);
    }
}
