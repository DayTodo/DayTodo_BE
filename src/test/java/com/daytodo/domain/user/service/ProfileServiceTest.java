package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.domain.user.storage.ProfileImageStorage;
import com.daytodo.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock UserRepository userRepository;
    @Mock ProfileImageStorage profileImageStorage;

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void updatesNicknameWithoutTreatingCurrentUserAsDuplicate() {
        User user = user();
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByNicknameAndIdNot("새 닉네임", 1L)).thenReturn(false);

        ProfileService service = new ProfileService(userRepository, profileImageStorage);
        UserResponse.Profile response = service.updateProfile(1L, " 새 닉네임 ", null);

        assertThat(response.nickname()).isEqualTo("새 닉네임");
        assertThat(response.profileImageUrl()).isEqualTo("old-url");
        verify(profileImageStorage, never()).upload(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsDuplicatedNicknameBeforeUploadingImage() {
        User user = user();
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByNicknameAndIdNot("중복", 1L)).thenReturn(true);

        ProfileService service = new ProfileService(userRepository, profileImageStorage);

        assertThatThrownBy(() -> service.updateProfile(
                1L,
                "중복",
                new MockMultipartFile("profileImage", "a.jpg", "image/jpeg", new byte[]{1})
        )).isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.NICKNAME_DUPLICATED);
        verify(profileImageStorage, never()).upload(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deletesPreviousImageOnlyAfterCommit() {
        User user = user();
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(profileImageStorage.upload(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn("new-url");
        TransactionSynchronizationManager.initSynchronization();

        ProfileService service = new ProfileService(userRepository, profileImageStorage);
        service.updateProfile(
                1L,
                "daytodo",
                new MockMultipartFile("profileImage", "a.jpg", "image/jpeg", new byte[]{1})
        );

        verify(profileImageStorage, never()).deleteByUrl("old-url");
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        verify(profileImageStorage).deleteByUrl("old-url");
    }

    @Test
    void removesNewImageWhenTransactionRollsBack() {
        User user = user();
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(profileImageStorage.upload(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn("new-url");
        TransactionSynchronizationManager.initSynchronization();

        ProfileService service = new ProfileService(userRepository, profileImageStorage);
        service.updateProfile(
                1L,
                "daytodo",
                new MockMultipartFile("profileImage", "a.jpg", "image/jpeg", new byte[]{1})
        );

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verify(profileImageStorage).deleteByUrl("new-url");
        verify(profileImageStorage, never()).deleteByUrl("old-url");
    }

    private User user() {
        User user = new User("user@example.com", "password", "daytodo", "old-url", LoginType.LOCAL);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
