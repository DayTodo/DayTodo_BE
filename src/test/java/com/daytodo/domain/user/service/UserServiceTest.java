package com.daytodo.domain.user.service;

import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.enums.RegionLevel;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-22T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock UserRepository userRepository;
    @Mock UserInterestRegionRepository interestRegionRepository;
    @Mock RegionRepository regionRepository;

    UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, interestRegionRepository, regionRepository, CLOCK);
    }

    @Test
    void authenticatedUserCanReadOwnProfile() {
        User user = user(1L, UserStatus.ACTIVE);
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));

        UserResponse.Profile response = userService.getProfile(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("daytodo");
        assertThat(response.profileImageUrl()).isEqualTo("profile.png");
    }

    @Test
    void withdrawnUserCannotReadProfile() {
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(1L))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void replacesAllInterestRegions() {
        User user = user(1L, UserStatus.ACTIVE);
        Region seoul = region(10L, null, "서울특별시", RegionLevel.SIDO);
        Region gangnam = region(11L, seoul, "강남구", RegionLevel.SIGUNGU);
        Region songpa = region(12L, seoul, "송파구", RegionLevel.SIGUNGU);
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(regionRepository.findAllByRegionIdIn(anyList())).thenReturn(List.of(gangnam, songpa));

        UserResponse.InterestRegions response = userService.replaceInterestRegions(
                1L,
                new UserRequest.ReplaceInterestRegions(List.of(11L, 12L))
        );

        verify(interestRegionRepository).deleteAllByUserId(1L);
        verify(interestRegionRepository).saveAll(anyList());
        assertThat(response.regions()).extracting(UserResponse.InterestRegion::regionId)
                .containsExactly(11L, 12L);
    }

    @Test
    void readsMultipleInterestRegions() {
        User user = user(1L, UserStatus.ACTIVE);
        Region seoul = region(10L, null, "서울특별시", RegionLevel.SIDO);
        Region gangnam = region(11L, seoul, "강남구", RegionLevel.SIGUNGU);
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(interestRegionRepository.findAllByUserIdOrderByIdAsc(1L))
                .thenReturn(List.of(new UserInterestRegion(user, 11L)));
        when(regionRepository.findAllByRegionIdIn(List.of(11L))).thenReturn(List.of(gangnam));

        UserResponse.InterestRegions response = userService.getInterestRegions(1L);

        assertThat(response.regions()).hasSize(1);
        assertThat(response.regions().get(0).parentRegionName()).isEqualTo("서울특별시");
    }

    @Test
    void rejectsUnknownInterestRegion() {
        User user = user(1L, UserStatus.ACTIVE);
        when(userRepository.findByIdAndUserStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(regionRepository.findAllByRegionIdIn(List.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> userService.replaceInterestRegions(
                1L,
                new UserRequest.ReplaceInterestRegions(List.of(999L))
        )).isInstanceOf(ProjectException.class);
    }

    @Test
    void withdrawsUserWithTimestamp() {
        User user = user(1L, UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.withdraw(1L);

        assertThat(user.getUserStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.getWithdrawnAt()).isEqualTo(LocalDateTime.of(2026, 7, 22, 10, 0));
    }

    @Test
    void rejectsRepeatedWithdrawal() {
        User user = user(1L, UserStatus.WITHDRAWN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.withdraw(1L))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_ALREADY_WITHDRAWN);
    }

    private User user(Long id, UserStatus status) {
        User user = new User("user@example.com", "password", "daytodo", "profile.png", LoginType.LOCAL);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "userStatus", status);
        return user;
    }

    private Region region(Long id, Region parent, String name, RegionLevel level) {
        Region region = new Region(parent, name, level);
        ReflectionTestUtils.setField(region, "regionId", id);
        return region;
    }
}
