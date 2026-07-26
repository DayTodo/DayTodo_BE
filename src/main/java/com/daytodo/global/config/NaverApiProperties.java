package com.daytodo.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver")
public record NaverApiProperties(
        String baseUrl,
        String clientId,
        String clientSecret
) {
}