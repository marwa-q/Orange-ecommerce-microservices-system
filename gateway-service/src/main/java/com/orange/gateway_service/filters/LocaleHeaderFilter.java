package com.orange.gateway_service.filters;

import com.orange.gateway_service.config.GatewayAppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter to ensure Accept-Language header is forwarded to downstream services.
 * Reads Accept-Language from incoming request and injects it if missing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocaleHeaderFilter implements GlobalFilter, Ordered {

    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    private final GatewayAppProperties gatewayProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        // Get Accept-Language from incoming request
        String acceptLanguage = headers.getFirst(ACCEPT_LANGUAGE_HEADER);

        // Get default language from configuration
        String defaultLanguage = gatewayProperties.getLocale() != null
            ? gatewayProperties.getLocale().getDefaultLanguage()
            : "en";

        // If missing, set default language from configuration
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            acceptLanguage = defaultLanguage;
            log.debug("Accept-Language header missing, setting default: {}", defaultLanguage);
        } else {
            log.debug("Accept-Language header found: {}", acceptLanguage);
        }

        // Ensure Accept-Language is forwarded to downstream services
        ServerHttpRequest mutatedRequest = request.mutate()
            .header(ACCEPT_LANGUAGE_HEADER, acceptLanguage)
            .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
            .request(mutatedRequest)
            .build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        // Execute early, after ExchangeContextFilter but before authentication filters
        return -150;
    }
}

