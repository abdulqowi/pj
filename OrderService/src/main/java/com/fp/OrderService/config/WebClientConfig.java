package com.fp.OrderService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${get.baseurl}")
    private String getBaseUrl;

    @Bean
    public WebClient genericWebClient() {
        return WebClient.builder()
                .baseUrl(getBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
                ).build();
    }
}
