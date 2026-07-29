package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserRequest;
import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.entity.UserNotificationSetting;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.domain.user.repository.UserNotificationSettingRepository;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserNotificationService {

    private final UserRepository userRepository;
    private final UserNotificationSettingRepository settingRepository;

    public UserResponse.NotificationSettings getSettings(Long userId) {
        requireActiveUser(userId);
        return settingRepository.findByUser_Id(userId)
                .map(this::toResponse)
                .orElseGet(() -> new UserResponse.NotificationSettings(true, true, true));
    }

    @Transactional
    public UserResponse.NotificationSettings updateSettings(
            Long userId,
            UserRequest.UpdateNotificationSettings request
    ) {
        User user = userRepository.findActiveUserForUpdate(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
        UserNotificationSetting setting = settingRepository.findByUser_Id(userId)
                .orElseGet(() -> new UserNotificationSetting(user));
        setting.updateSettings(
                request.pushEnabled(),
                request.courseD1Enabled(),
                request.courseD0Enabled()
        );
        if (setting.getId() == null) {
            settingRepository.save(setting);
        }
        return toResponse(setting);
    }

    private void requireActiveUser(Long userId) {
        userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
    }

    private UserResponse.NotificationSettings toResponse(UserNotificationSetting setting) {
        return new UserResponse.NotificationSettings(
                setting.isPushEnabled(),
                setting.isCourseD1Enabled(),
                setting.isCourseD0Enabled()
        );
    }
}
