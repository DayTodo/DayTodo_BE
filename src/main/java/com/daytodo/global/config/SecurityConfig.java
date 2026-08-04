package com.daytodo.global.config;

import com.daytodo.global.apiPayload.ErrorResponse;
import com.daytodo.global.apiPayload.code.GeneralErrorCode;
import com.daytodo.global.security.JwtAuthenticationFilter;
import com.daytodo.global.security.JwtTokenProvider;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

/**
 * JWT 기반 인증 적용.
 * User API와 일부 Course API, Diary API는 JWT 인증으로 전환했습니다.
 * 아직 전환하지 않은 Course API만 LEGACY_PERMIT_ALL_COURSE_PATHS에서 임시로 허용합니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // auth 쪽은 로그인 전에도 호출해야 하는 엔드포인트만 개별로 열어뒀습니다.
    // /auth/link/naver는 로그인한 사용자만 써야 하므로 의도적으로 목록에서 제외했습니다.
    private static final String[] PERMIT_ALL_PATHS = {
            "/auth/login",
            "/auth/login/naver",
            "/auth/register",
            "/auth/email-check",
            "/auth/token/refresh",
            "/auth/logout",
            "/auth/verify-email",
            "/auth/verify-email/resend",
            "/auth/password/reset-request",
            "/auth/password/reset",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/health"
    };

    // JWT 전환 대상 외의 기존 Course API 접근 정책은 변경하지 않는다.
    private static final String[] LEGACY_PERMIT_ALL_COURSE_PATHS = {
            "/courses/today",
            "/courses/*/complete",
            "/courses/*/photos",
            "/courses/*/setting",
            "/courses/*/places",
            "/courses/*/members",
            "/courses/*/members/*"
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Diary는 JWT 인증 전환 완료
                        .requestMatchers(HttpMethod.POST, "/courses/diaries").authenticated()
                        .requestMatchers(HttpMethod.GET, "/courses/diaries/calendar").authenticated()
                        .requestMatchers(HttpMethod.GET, "/courses/diaries/{diaryId}/course").authenticated()
                        .requestMatchers(HttpMethod.GET, "/courses/diaries").authenticated()
                        .requestMatchers(HttpMethod.GET, "/courses/*/memory-photos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/policies").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/users/profile",
                                "/users/interest-region",
                                "/users/notifications",
                                "/courses",
                                "/courses/calendar"
                        ).authenticated()
                        .requestMatchers(HttpMethod.PATCH,
                                "/users/profile",
                                "/users/interest-regions",
                                "/users/notifications"
                        ).authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.POST,
                                "/users/feedback",
                                "/courses",
                                "/courses/join"
                        ).authenticated()
                        // AI 추천 기능의 수동 테스트가 끝나면 이 허용 규칙을 제거하고 JWT 인증으로 되돌린다.
                        .requestMatchers(HttpMethod.POST, "/courses/ai-recommendations").permitAll()
                        .requestMatchers(LEGACY_PERMIT_ALL_COURSE_PATHS).permitAll()
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeSecurityError(response, GeneralErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) ->
                                writeSecurityError(response, GeneralErrorCode.FORBIDDEN))
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // allowCredentials(true)라 "*"는 못 쓰고 패턴을 사용 - 요청 Origin을 그대로 반사한다.
        // TODO(팀 확인 필요): 배포 안정화되면 프론트 도메인만 명시적으로 화이트리스트할 것.
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void writeSecurityError(
            jakarta.servlet.http.HttpServletResponse response,
            GeneralErrorCode errorCode
    ) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(errorCode));
    }
}
