package com.daytodo.domain.user.dto;

import java.util.List;

public final class UserResponse {
    private UserResponse() {
    }

    public record Profile(Long userId, String email, String nickname, String profileImageUrl) {
    }

    public record InterestRegion(Long regionId, String regionName, String parentRegionName) {
    }

    public record InterestRegions(List<InterestRegion> regions) {
        public InterestRegions {
            regions = List.copyOf(regions);
        }
    }

    public record NotificationSettings(
            boolean pushEnabled
    ) {
    }

    public record Policies(String termsOfService, String privacyPolicy) {
    }
}
