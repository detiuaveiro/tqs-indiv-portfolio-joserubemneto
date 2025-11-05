package com.zeremonos.wastecollection.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${geoapi.base-url}")
    private String geoApiBaseUrl;

    @Bean
    public WebClient geoApiWebClient() {
        return WebClient.builder()
                .baseUrl(geoApiBaseUrl)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}