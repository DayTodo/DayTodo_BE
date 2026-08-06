package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.LoginType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.test.database.replace=none")
class FcmTokenRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired FcmTokenRepository fcmTokenRepository;
    @Autowired EntityManager entityManager;

    @Test
    void upsertMovesExistingTokenToCurrentUser() {
        User firstUser = userRepository.save(new User(
                "first@example.com", "password", "first", null, LoginType.LOCAL));
        User secondUser = userRepository.save(new User(
                "second@example.com", "password", "second", null, LoginType.LOCAL));
        userRepository.flush();

        fcmTokenRepository.upsert(firstUser.getId(), "token", "ANDROID");
        fcmTokenRepository.upsert(secondUser.getId(), "token", "IOS");
        entityManager.clear();

        var token = fcmTokenRepository.findByToken("token").orElseThrow();
        assertThat(token.getUser().getId()).isEqualTo(secondUser.getId());
        assertThat(token.getPlatform().name()).isEqualTo("IOS");
        assertThat(fcmTokenRepository.count()).isEqualTo(1);
    }
}
