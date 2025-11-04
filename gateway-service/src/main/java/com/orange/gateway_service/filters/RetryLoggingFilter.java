package com.orange.gateway_service.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RetryLoggingFilter implements GlobalFilter, Ordered {

    private static final String RETRY_ATTEMPT_KEY = "gatewayRetryAttempt";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Integer attempt = exchange.getAttribute(RETRY_ATTEMPT_KEY);
        int nextAttempt = attempt == null ? 1 : attempt + 1;
        exchange.getAttributes().put(RETRY_ATTEMPT_KEY, nextAttempt);

        ServerHttpRequest request = exchange.getRequest();
        if (nextAttempt > 1) {
            log.warn("Retry attempt {} for {} {}", nextAttempt - 1, request.getMethod(), request.getURI().getPath());
        } else {
            log.debug("Initial attempt for {} {}", request.getMethod(), request.getURI().getPath());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -150;
    }
}


