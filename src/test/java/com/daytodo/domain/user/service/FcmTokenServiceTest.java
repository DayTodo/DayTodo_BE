package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserRequest;
import com.daytodo.domain.user.entity.FcmToken;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.DevicePlatform;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.repository.FcmTokenRepository;
import com.daytodo.domain.user.repository.UserNotificationSettingRepository;
import com.daytodo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FcmTokenServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserNotificationSettingRepository settingRepository;
    @Mock FcmTokenRepository fcmTokenRepository;

    @Test
    void registersNewTokenAndCreatesDefaultSettings() {
        User user = user(1L);
        when(userRepository.findActiveUserForUpdate(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(settingRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(fcmTokenRepository.findByToken("token")).thenReturn(Optional.empty());

        FcmTokenService service = new FcmTokenService(userRepository, settingRepository, fcmTokenRepository);
        service.register(1L, new UserRequest.RegisterFcmToken("token", DevicePlatform.ANDROID));

        verify(settingRepository).save(org.mockito.ArgumentMatchers.any());
        ArgumentCaptor<FcmToken> captor = ArgumentCaptor.forClass(FcmToken.class);
        verify(fcmTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getToken()).isEqualTo("token");
        assertThat(captor.getValue().getPlatform()).isEqualTo(DevicePlatform.ANDROID);
    }

    @Test
    void movesExistingTokenToCurrentUser() {
        User previousUser = user(2L);
        User currentUser = user(1L);
        FcmToken token = new FcmToken(previousUser, "token", DevicePlatform.ANDROID);
        when(userRepository.findActiveUserForUpdate(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(currentUser));
        when(settingRepository.findByUser_Id(1L))
                .thenReturn(Optional.of(new com.daytodo.domain.user.entity.UserNotificationSetting(currentUser)));
        when(fcmTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        FcmTokenService service = new FcmTokenService(userRepository, settingRepository, fcmTokenRepository);
        service.register(1L, new UserRequest.RegisterFcmToken("token", DevicePlatform.IOS));

        assertThat(token.getUser()).isSameAs(currentUser);
        assertThat(token.getPlatform()).isEqualTo(DevicePlatform.IOS);
    }

    @Test
    void deletesOnlyTokenOwnedByCurrentUser() {
        User user = user(1L);
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));

        FcmTokenService service = new FcmTokenService(userRepository, settingRepository, fcmTokenRepository);
        service.delete(1L, new UserRequest.DeleteFcmToken("token"));

        verify(fcmTokenRepository).deleteByTokenAndUser_Id("token", 1L);
    }

    private User user(Long id) {
        User user = new User("user" + id + "@example.com", "password", "user" + id, null, LoginType.LOCAL);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
