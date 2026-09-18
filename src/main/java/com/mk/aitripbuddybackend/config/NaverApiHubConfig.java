package com.mk.aitripbuddybackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.api-hub")
public record NaverApiHubConfig(
        String clientId,
        String clientSecret
) {
}