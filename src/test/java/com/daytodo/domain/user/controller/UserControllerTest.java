package com.daytodo.domain.user.controller;

import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.service.UserService;
import com.daytodo.domain.user.service.ProfileService;
import com.daytodo.domain.user.service.UserNotificationService;
import com.daytodo.domain.user.service.FeedbackService;
import com.daytodo.domain.user.service.PolicyService;
import com.daytodo.domain.user.service.FcmTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock UserService userService;
    @Mock ProfileService profileService;
    @Mock UserNotificationService notificationService;
    @Mock FeedbackService feedbackService;
    @Mock PolicyService policyService;
    @Mock FcmTokenService fcmTokenService;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(
                        userService,
                        profileService,
                        notificationService,
                        feedbackService,
                        policyService,
                        fcmTokenService
                ))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void delegatesUsingAuthenticatedPrincipal() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null)
        );
        when(userService.getProfile(1L)).thenReturn(
                new UserResponse.Profile(1L, "user@example.com", "daytodo", "profile.png")
        );

        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
        verify(userService).getProfile(1L);
    }

    @Test
    void registersFcmTokenUsingAuthenticatedPrincipal() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null)
        );

        mockMvc.perform(post("/users/fcm-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"device-token\",\"platform\":\"ANDROID\"}"))
                .andExpect(status().isNoContent());

        verify(fcmTokenService).register(
                1L,
                new com.daytodo.domain.user.dto.UserRequest.RegisterFcmToken(
                        "device-token",
                        com.daytodo.domain.user.enums.DevicePlatform.ANDROID
                )
        );
    }

    @Test
    void changesPasswordUsingAuthenticatedPrincipal() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null)
        );

        mockMvc.perform(patch("/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"current1234\",\"newPassword\":\"newPassword1234\"}"))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(
                1L,
                new com.daytodo.domain.user.dto.UserRequest.ChangePassword("current1234", "newPassword1234")
        );
    }

    @Test
    void deletesFcmTokenUsingAuthenticatedPrincipal() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null)
        );

        mockMvc.perform(delete("/users/fcm-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"device-token\"}"))
                .andExpect(status().isNoContent());

        verify(fcmTokenService).delete(
                1L,
                new com.daytodo.domain.user.dto.UserRequest.DeleteFcmToken("device-token")
        );
    }
}
