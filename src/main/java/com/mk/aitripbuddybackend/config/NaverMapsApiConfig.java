package com.mk.aitripbuddybackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.maps")
public record NaverMapsApiConfig(
        String clientId,
        String clientSecret
) {
}