package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.entity.UserNotificationSetting;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryQueryValidationTest {

    @Autowired UserRepository userRepository;
    @Autowired UserNotificationSettingRepository settingRepository;
    @Autowired FeedbackRepository feedbackRepository;

    @Test
    void derivedAndExplicitQueriesUseActualUserIdField() {
        User user = userRepository.save(new User(
                "query@example.com",
                "password",
                "query-user",
                null,
                LoginType.LOCAL
        ));

        assertThat(userRepository.existsByNicknameAndIdNot("query-user", -1L)).isTrue();
        assertThat(userRepository.findActiveUserForUpdate(user.getId(), UserStatus.ACTIVE))
                .contains(user);

        settingRepository.save(new UserNotificationSetting(user));
        assertThat(settingRepository.findByUser_Id(user.getId())).isPresent();
        assertThat(feedbackRepository.count()).isZero();
    }
}
