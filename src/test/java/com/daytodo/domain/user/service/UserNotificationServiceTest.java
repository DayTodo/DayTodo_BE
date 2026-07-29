package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserRequest;
import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.entity.UserNotificationSetting;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.repository.UserNotificationSettingRepository;
import com.daytodo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserNotificationSettingRepository settingRepository;

    @Test
    void getReturnsDefaultsWithoutSavingWhenSettingDoesNotExist() {
        User user = user();
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(settingRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        UserNotificationService service = new UserNotificationService(userRepository, settingRepository);
        UserResponse.NotificationSettings response = service.getSettings(1L);

        assertThat(response).isEqualTo(new UserResponse.NotificationSettings(true, true, true));
        verify(settingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void patchCreatesMissingSettingWithRequestedValues() {
        User user = user();
        when(userRepository.findActiveUserForUpdate(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(settingRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        UserNotificationService service = new UserNotificationService(userRepository, settingRepository);
        UserResponse.NotificationSettings response = service.updateSettings(
                1L,
                new UserRequest.UpdateNotificationSettings(false, true, false)
        );

        assertThat(response).isEqualTo(new UserResponse.NotificationSettings(false, true, false));
        verify(settingRepository).save(org.mockito.ArgumentMatchers.any(UserNotificationSetting.class));
    }

    private User user() {
        User user = new User("user@example.com", "password", "daytodo", null, LoginType.LOCAL);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
