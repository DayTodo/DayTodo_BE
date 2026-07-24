package com.daytodo.global.config;

import com.daytodo.global.security.JwtAuthenticationFilter;
import com.daytodo.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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

/**
 * JWT 기반 인증 적용.
 * TODO(팀 확인 필요): Course/User 컨트롤러가 아직 X-User-Id 임시 헤더를 쓰고 있어서
 * /courses/**, /users/** 를 임시로 permitAll 에 넣어뒀습니다.
 * 팀 전체가 JWT 인증으로 전환하는 시점에 이 목록에서 빼야 합니다.
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
            "/courses/**",
            "/users/**"
    };

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated()
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
}