package com.mk.aitripbuddybackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.api")
public record NaverApiConfig(
        String clientId,
        String clientSecret
) {
}