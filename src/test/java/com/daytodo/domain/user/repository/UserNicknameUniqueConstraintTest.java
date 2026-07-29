package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.LoginType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserNicknameUniqueConstraintTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void databaseRejectsDuplicatedNickname() {
        userRepository.saveAndFlush(new User(
                "first@example.com",
                "password",
                "duplicate",
                null,
                LoginType.LOCAL
        ));

        assertThatThrownBy(() -> userRepository.saveAndFlush(new User(
                "second@example.com",
                "password",
                "duplicate",
                null,
                LoginType.LOCAL
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }
}
