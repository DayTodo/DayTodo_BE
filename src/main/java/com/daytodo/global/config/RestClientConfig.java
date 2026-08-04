package com.daytodo.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({NaverApiProperties.class, TourApiProperties.class})
public class RestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    public RestClient naverRestClient(NaverApiProperties properties) {
        return RestClient.builder()
                .requestFactory(timeoutRequestFactory())
                .baseUrl(properties.baseUrl())
                .defaultHeader("X-Naver-Client-Id", properties.clientId())
                .defaultHeader("X-Naver-Client-Secret", properties.clientSecret())
                .build();
    }

    // 한국관광공사 KorService2. serviceKey 는 각 호출에서 쿼리파라미터로 붙인다.
    @Bean
    public RestClient tourRestClient(TourApiProperties properties) {
        return RestClient.builder()
                .requestFactory(timeoutRequestFactory())
                .baseUrl(properties.baseUrl())
                .build();
    }

    // 외부 API 지연 시 요청 스레드가 장시간 점유되지 않도록 connect/read timeout 을 명시한다.
    private static ClientHttpRequestFactory timeoutRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        return factory;
    }
}