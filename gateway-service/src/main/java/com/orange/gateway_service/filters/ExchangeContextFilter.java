package com.orange.gateway_service.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ExchangeContextFilter implements GlobalFilter, Ordered {

    public static final String EXCHANGE_CONTEXT_KEY = "CURRENT_EXCHANGE";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Set the exchange in the Reactor context BEFORE the chain runs
        return Mono.deferContextual(ctxView -> 
            chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(EXCHANGE_CONTEXT_KEY, exchange))
        );
    }

    @Override
    public int getOrder() {
        return -2000; // high priority so it's available to all filters/controllers
    }
}
