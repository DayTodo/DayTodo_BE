package com.daytodo.domain.user.controller;

import com.daytodo.domain.user.dto.UserRequest;
import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.service.FeedbackService;
import com.daytodo.domain.user.service.PolicyService;
import com.daytodo.domain.user.service.ProfileService;
import com.daytodo.domain.user.service.UserNotificationService;
import com.daytodo.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ProfileService profileService;
    private final UserNotificationService notificationService;
    private final FeedbackService feedbackService;
    private final PolicyService policyService;

    @Operation(summary = "프로필 조회")
    @GetMapping("/profile")
    public UserResponse.Profile getProfile(
            @AuthenticationPrincipal Long userId
    ) {
        return userService.getProfile(userId);
    }

    @Operation(summary = "프로필 수정")
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse.Profile updateProfile(
            @AuthenticationPrincipal Long userId,
            @RequestPart("nickname") String nickname,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return profileService.updateProfile(userId, nickname, profileImage);
    }

    @Operation(summary = "관심지역 조회")
    @GetMapping("/interest-region")
    public UserResponse.InterestRegions getInterestRegions(
            @AuthenticationPrincipal Long userId
    ) {
        return userService.getInterestRegions(userId);
    }

    @Operation(summary = "관심지역 전체 변경")
    @PatchMapping("/interest-regions")
    public UserResponse.InterestRegions replaceInterestRegions(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequest.ReplaceInterestRegions request
    ) {
        return userService.replaceInterestRegions(userId, request);
    }

    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal Long userId
    ) {
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "알림 설정 조회")
    @GetMapping("/notifications")
    public UserResponse.NotificationSettings getNotificationSettings(
            @AuthenticationPrincipal Long userId
    ) {
        return notificationService.getSettings(userId);
    }

    @Operation(summary = "알림 설정 변경")
    @PatchMapping("/notifications")
    public UserResponse.NotificationSettings updateNotificationSettings(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequest.UpdateNotificationSettings request
    ) {
        return notificationService.updateSettings(userId, request);
    }

    @Operation(summary = "의견 보내기")
    @PostMapping("/feedback")
    public ResponseEntity<Void> submitFeedback(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequest.SubmitFeedback request
    ) {
        feedbackService.submit(userId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "약관 및 정책 조회")
    @GetMapping("/policies")
    public UserResponse.Policies getPolicies() {
        return policyService.getPolicies();
    }
}
