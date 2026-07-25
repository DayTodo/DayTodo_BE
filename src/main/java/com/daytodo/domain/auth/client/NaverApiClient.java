package com.daytodo.domain.auth.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 네이버 로그인 API 연동.
 * 클라이언트(앱)에서 네이버 OAuth로 발급받은 accessToken을 그대로 전달받아
 * 네이버 프로필 조회 API를 대신 호출하는 방식입니다.
 * TODO(팀 확인 필요): 프론트-백엔드 간 네이버 OAuth 플로우 분담 방식이 이 방식이 맞는지 확인 필요.
 */
@Component
public class NaverApiClient {

    private static final String NAVER_PROFILE_URL = "https://openapi.naver.com/v1/nid/me";
    private static final int CONNECT_TIMEOUT_MILLIS = 3000;
    private static final int READ_TIMEOUT_MILLIS = 5000;

    private final RestClient restClient = RestClient.builder()
            .requestFactory(clientHttpRequestFactory())
            .build();

    public NaverProfileResponse getProfile(String naverAccessToken) {
        return restClient.get()
                .uri(NAVER_PROFILE_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + naverAccessToken)
                .retrieve()
                .body(NaverProfileResponse.class);
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        factory.setReadTimeout(READ_TIMEOUT_MILLIS);
        return factory;
    }

    public record NaverProfileResponse(String resultcode, String message, Response response) {

        public record Response(String id, String email, String nickname) {
        }
    }
}