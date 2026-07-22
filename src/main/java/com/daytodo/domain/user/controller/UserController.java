package com.daytodo.domain.user.controller;

import com.daytodo.domain.user.dto.UserRequest;
import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private static final String TEMPORARY_USER_ID_HEADER = "X-User-Id";

    private final UserService userService;

    @Operation(summary = "프로필 조회")
    @GetMapping("/profile")
    public UserResponse.Profile getProfile(
            // TODO Auth 구현 후 인증 Principal에서 userId를 주입하도록 교체한다.
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId
    ) {
        return userService.getProfile(userId);
    }

    @Operation(summary = "관심지역 조회")
    @GetMapping("/interest-region")
    public UserResponse.InterestRegions getInterestRegions(
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId
    ) {
        return userService.getInterestRegions(userId);
    }

    @Operation(summary = "관심지역 전체 변경")
    @PatchMapping("/interest-regions")
    public UserResponse.InterestRegions replaceInterestRegions(
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId,
            @Valid @RequestBody UserRequest.ReplaceInterestRegions request
    ) {
        return userService.replaceInterestRegions(userId, request);
    }

    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @RequestHeader(TEMPORARY_USER_ID_HEADER) Long userId
    ) {
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }
}
