package com.orange.gateway_service.bff.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * BFF WebClient configuration.
 * Uses Spring Boot's auto-configured WebClient.Builder to ensure
 * shared Netty resources with Spring Cloud Gateway, preventing
 * event loop conflicts during shutdown.
 */
@Configuration
public class BffWebClientConfig {

    /**
     * Create WebClient bean using Spring Boot's auto-configured builder.
     * The builder is configured by Spring Boot to use shared Netty resources,
     * preventing conflicts with Spring Cloud Gateway's event loop.
     */
    @Bean
    @ConditionalOnMissingBean
    public WebClient webClient(org.springframework.web.reactive.function.client.WebClient.Builder builder) {
        // Spring Boot's auto-configured builder uses shared Netty resources
        // This prevents creating a separate event loop
        return builder.build();
    }
}


