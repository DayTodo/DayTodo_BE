package com.daytodo.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({NaverApiProperties.class, TourApiProperties.class})
public class RestClientConfig {

    @Bean
    public RestClient naverRestClient(NaverApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("X-Naver-Client-Id", properties.clientId())
                .defaultHeader("X-Naver-Client-Secret", properties.clientSecret())
                .build();
    }

    // 한국관광공사 KorService2. serviceKey 는 각 호출에서 쿼리파라미터로 붙인다.
    @Bean
    public RestClient tourRestClient(TourApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }
}