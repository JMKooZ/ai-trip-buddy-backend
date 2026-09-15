package com.mk.aitripbuddybackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NaverApiClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}