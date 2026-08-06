package com.daytodo.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 한국관광공사 국문 관광정보 서비스(KorService2) 연동 설정.
 * serviceKey 는 공공데이터포털에서 발급받은 인코딩키를 그대로 사용한다.
 */
@ConfigurationProperties(prefix = "tour")
public record TourApiProperties(
        String baseUrl,
        String serviceKey
) {
}
