package com.orange.gateway_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Gateway Service API",
                description = "API Gateway for Orange E-commerce Microservices System - Reverse Proxy and API Management"
        )
)
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi userServiceApi() {
        return GroupedOpenApi.builder()
                .group("user-service")
                .displayName("User Service APIs")
                .pathsToMatch("/api/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cartServiceApi() {
        return GroupedOpenApi.builder()
                .group("cart-service")
                .displayName("Cart Service APIs")
                .pathsToMatch("/api/cart/**")
                .build();
    }

    @Bean
    public GroupedOpenApi productServiceApi() {
        return GroupedOpenApi.builder()
                .group("product-service")
                .displayName("Product Service APIs")
                .pathsToMatch("/api/products/**", "/api/categories/**", "/api/reviews/**", "/api/tags/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderServiceApi() {
        return GroupedOpenApi.builder()
                .group("order-service")
                .displayName("Order Service APIs")
                .pathsToMatch("/api/orders/**")
                .build();
    }
}
