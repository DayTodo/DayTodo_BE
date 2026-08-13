package com.daytodo.domain.user.service;

import com.daytodo.domain.auth.repository.PasswordResetTokenRepository;
import com.daytodo.domain.auth.repository.RefreshTokenRepository;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.exception.code.RegionErrorCode;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.domain.user.dto.UserRequest;
import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.entity.mapping.UserInterestRegion;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.domain.user.repository.UserInterestRegionRepository;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserInterestRegionRepository interestRegionRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final Clock clock;

    public UserResponse.Profile getProfile(Long userId) {
        User user = getActiveUser(userId);
        return new UserResponse.Profile(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

    public UserResponse.InterestRegions getInterestRegions(Long userId) {
        getActiveUser(userId);
        List<UserInterestRegion> interests = interestRegionRepository.findAllByUserIdOrderByIdAsc(userId);
        Map<Long, Region> regions = regionsById(
                interests.stream().map(UserInterestRegion::getRegionId).toList()
        );
        List<UserResponse.InterestRegion> responses = interests.stream()
                .map(interest -> toInterestRegion(regions.get(interest.getRegionId())))
                .toList();
        return new UserResponse.InterestRegions(responses);
    }

    @Transactional
    public UserResponse.InterestRegions replaceInterestRegions(
            Long userId,
            UserRequest.ReplaceInterestRegions request
    ) {
        User user = getActiveUser(userId);
        if (new HashSet<>(request.regionIds()).size() != request.regionIds().size()) {
            throw new ProjectException(RegionErrorCode.DUPLICATE_REGION);
        }

        Map<Long, Region> regions = regionsById(request.regionIds());
        interestRegionRepository.deleteAllByUserId(userId);
        interestRegionRepository.saveAll(
                request.regionIds().stream()
                        .map(regionId -> new UserInterestRegion(user, regionId))
                        .toList()
        );

        return new UserResponse.InterestRegions(
                request.regionIds().stream()
                        .map(regions::get)
                        .map(this::toInterestRegion)
                        .toList()
        );
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
        if (user.getUserStatus() == UserStatus.WITHDRAWN) {
            throw new ProjectException(UserErrorCode.USER_ALREADY_WITHDRAWN);
        }
        user.withdraw(LocalDateTime.now(clock));
    }

    @Transactional
    public void changePassword(Long userId, UserRequest.ChangePassword request) {
        User user = userRepository.findActiveUserForUpdate(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
        // 네이버 전용 계정(password=null)은 현재 비밀번호가 없으므로 마이페이지에서 변경할 수 없다.
        if (user.getLoginType() != LoginType.LOCAL) {
            throw new ProjectException(UserErrorCode.SOCIAL_ACCOUNT_PASSWORD_CHANGE_NOT_ALLOWED);
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ProjectException(UserErrorCode.INVALID_CURRENT_PASSWORD);
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        // 비밀번호 변경 시 기존에 발급된 refresh token(탈취 가능성 있는 세션)과
        // 대기 중이던 비밀번호 재설정 코드를 함께 무효화한다.
        refreshTokenRepository.findById(userId).ifPresent(refreshTokenRepository::delete);
        passwordResetTokenRepository.findById(userId).ifPresent(passwordResetTokenRepository::delete);
    }

    public User getActiveUser(Long userId) {
        return userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
    }

    private Map<Long, Region> regionsById(List<Long> regionIds) {
        if (regionIds.isEmpty()) {
            return Map.of();
        }
        List<Region> regions = regionRepository.findAllByRegionIdIn(regionIds);
        if (regions.size() != new HashSet<>(regionIds).size()) {
            throw new ProjectException(RegionErrorCode.REGION_NOT_FOUND);
        }
        Map<Long, Region> result = new HashMap<>();
        regions.forEach(region -> result.put(region.getRegionId(), region));
        return result;
    }

    private UserResponse.InterestRegion toInterestRegion(Region region) {
        if (region == null) {
            throw new ProjectException(RegionErrorCode.REGION_NOT_FOUND);
        }
        return new UserResponse.InterestRegion(
                region.getRegionId(),
                region.getRegionName(),
                region.getParent() == null ? null : region.getParent().getRegionName()
        );
    }
}
