package com.daytodo.domain.user.service;

import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.exception.code.RegionErrorCode;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.domain.user.dto.UserRequest;
import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.entity.mapping.UserInterestRegion;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.domain.user.repository.UserInterestRegionRepository;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
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
    private final Clock clock;

    public UserResponse.Profile getProfile(Long userId) {
        User user = getActiveUser(userId);
        return new UserResponse.Profile(user.getId(), user.getNickname(), user.getProfileImageUrl());
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
