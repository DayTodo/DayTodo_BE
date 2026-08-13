package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.domain.user.storage.ProfileImageStorage;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final int MAX_NICKNAME_LENGTH = 20;

    private final UserRepository userRepository;
    private final ProfileImageStorage profileImageStorage;

    @Transactional
    public UserResponse.Profile updateProfile(
            Long userId,
            String nickname,
            MultipartFile profileImage
    ) {
        User user = userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
        String normalizedNickname = normalizeNickname(nickname);
        if (userRepository.existsByNicknameAndIdNot(normalizedNickname, userId)) {
            throw new ProjectException(UserErrorCode.NICKNAME_DUPLICATED);
        }

        String previousImageUrl = user.getProfileImageUrl();
        String newImageUrl = previousImageUrl;
        if (profileImage != null && !profileImage.isEmpty()) {
            newImageUrl = profileImageStorage.upload(userId, profileImage);
            registerImageCleanup(previousImageUrl, newImageUrl);
        }

        user.updateProfile(normalizedNickname, newImageUrl);
        try {
            userRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            if (isNicknameConstraintViolation(exception)) {
                throw new ProjectException(UserErrorCode.NICKNAME_DUPLICATED);
            }
            throw exception;
        }
        return new UserResponse.Profile(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

    private String normalizeNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new ProjectException(UserErrorCode.INVALID_NICKNAME);
        }
        String normalized = nickname.trim();
        if (normalized.length() > MAX_NICKNAME_LENGTH) {
            throw new ProjectException(UserErrorCode.INVALID_NICKNAME);
        }
        return normalized;
    }

    private void registerImageCleanup(String previousImageUrl, String newImageUrl) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (StringUtils.hasText(previousImageUrl) && !previousImageUrl.equals(newImageUrl)) {
                    deleteQuietly(previousImageUrl, "previous");
                }
            }

            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    deleteQuietly(newImageUrl, "new");
                }
            }
        });
    }

    private void deleteQuietly(String imageUrl, String target) {
        try {
            profileImageStorage.deleteByUrl(imageUrl);
        } catch (RuntimeException exception) {
            log.warn("Failed to delete {} profile image from S3", target, exception);
        }
    }

    private boolean isNicknameConstraintViolation(Throwable exception) {
        Throwable cause = exception;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.toLowerCase(java.util.Locale.ROOT)
                    .contains("uk_users_nickname")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
