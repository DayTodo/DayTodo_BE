package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserRequest;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.entity.UserNotificationSetting;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.domain.user.repository.FcmTokenRepository;
import com.daytodo.domain.user.repository.UserNotificationSettingRepository;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final UserRepository userRepository;
    private final UserNotificationSettingRepository settingRepository;
    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void register(Long userId, UserRequest.RegisterFcmToken request) {
        User user = userRepository.findActiveUserForUpdate(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        settingRepository.findByUser_Id(userId)
                .orElseGet(() -> settingRepository.save(new UserNotificationSetting(user)));

        fcmTokenRepository.upsert(user.getId(), request.token(), request.platform().name());
    }

    @Transactional
    public void delete(Long userId, UserRequest.DeleteFcmToken request) {
        requireActiveUser(userId);
        fcmTokenRepository.deleteByTokenAndUser_Id(request.token(), userId);
    }

    private void requireActiveUser(Long userId) {
        userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
    }
}
