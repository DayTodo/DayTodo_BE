package com.daytodo.global.config;

import com.daytodo.domain.course.controller.CourseController;
import com.daytodo.domain.course.dto.CourseResponse;
import com.daytodo.domain.course.service.CourseService;
import com.daytodo.domain.course.service.CourseAiRecommendationService;
import com.daytodo.domain.region.controller.RegionController;
import com.daytodo.domain.region.dto.RegionResponse;
import com.daytodo.domain.region.service.RegionService;
import com.daytodo.domain.user.controller.UserController;
import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.service.FeedbackService;
import com.daytodo.domain.user.service.PolicyService;
import com.daytodo.domain.user.service.ProfileService;
import com.daytodo.domain.user.service.UserNotificationService;
import com.daytodo.domain.user.service.UserService;
import com.daytodo.domain.user.service.FcmTokenService;
import com.daytodo.global.security.JwtTokenProvider;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, CourseController.class, RegionController.class})
@Import({
        SecurityConfig.class,
        JwtConfig.class,
        JwtTokenProvider.class,
        TargetApiSecurityTest.JsonTestConfiguration.class
})
@TestPropertySource(properties = {
        "jwt.token.secretKey=test-secret-key-test-secret-key-test-secret-key",
        "jwt.token.expiration.access=1800000",
        "jwt.token.expiration.refresh=1209600000"
})
class TargetApiSecurityTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @MockitoBean UserService userService;
    @MockitoBean ProfileService profileService;
    @MockitoBean UserNotificationService notificationService;
    @MockitoBean FeedbackService feedbackService;
    @MockitoBean PolicyService policyService;
    @MockitoBean FcmTokenService fcmTokenService;
    @MockitoBean CourseService courseService;
    @MockitoBean CourseAiRecommendationService courseAiRecommendationService;
    @MockitoBean RegionService regionService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void protectedUserApiRejectsRequestWithoutJwtInCommonErrorFormat() throws Exception {
        mockMvc.perform(get("/users/profile").header("X-User-Id", 2L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void jwtPrincipalOverridesUntrustedUserIdHeader() throws Exception {
        when(userService.getProfile(1L))
                .thenReturn(new UserResponse.Profile(1L, "user@example.com", "daytodo", null));

        mockMvc.perform(get("/users/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(1L))
                        .header("X-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L));

        verify(userService).getProfile(1L);
    }

    @Test
    void targetCourseApiRejectsRequestWithoutJwt() throws Exception {
        mockMvc.perform(post("/courses")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aiRecommendationApiRejectsRequestWithoutJwt() throws Exception {
        mockMvc.perform(post("/courses/ai-recommendations")
                        .contentType("application/json")
                        .content("{\"regionId\":1,\"minPrice\":10000,\"maxPrice\":30000}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void calendarUsesJwtPrincipal() throws Exception {
        when(courseService.getCalendar(1L, 2026, 7))
                .thenReturn(new CourseResponse.Calendar(2026, 7, List.of()));

        mockMvc.perform(get("/courses/calendar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(1L))
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isOk());

        verify(courseService).getCalendar(1L, 2026, 7);
    }

    @Test
    void policiesRemainPublic() throws Exception {
        when(policyService.getPolicies()).thenReturn(new UserResponse.Policies("terms", "privacy"));

        mockMvc.perform(get("/users/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.termsOfService").value("terms"));
    }

    @Test
    void regionsRemainPublic() throws Exception {
        when(regionService.getRegions()).thenReturn(new RegionResponse.Regions(List.of()));

        mockMvc.perform(get("/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regions").isArray())
                .andExpect(jsonPath("$.regions").isEmpty());

        verify(regionService).getRegions();
    }

    private String bearer(Long userId) {
        return "Bearer " + jwtTokenProvider.createAccessToken(userId);
    }

    @TestConfiguration
    static class JsonTestConfiguration {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
