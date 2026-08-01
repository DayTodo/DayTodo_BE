package com.daytodo.domain.user.controller;

import com.daytodo.domain.user.service.UserService;
import com.daytodo.domain.user.service.ProfileService;
import com.daytodo.domain.user.service.UserNotificationService;
import com.daytodo.domain.user.service.FeedbackService;
import com.daytodo.domain.user.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock UserService userService;
    @Mock ProfileService profileService;
    @Mock UserNotificationService notificationService;
    @Mock FeedbackService feedbackService;
    @Mock PolicyService policyService;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(
                userService,
                profileService,
                notificationService,
                feedbackService,
                policyService
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
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk());
        verify(userService).getProfile(1L);
    }
}
