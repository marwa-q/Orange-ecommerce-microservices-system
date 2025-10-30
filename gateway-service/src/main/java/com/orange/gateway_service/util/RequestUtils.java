package com.orange.gateway_service.util;

import com.orange.gateway_service.filters.ExchangeContextFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class RequestUtils {

    /**
     * Retrieves a header value from the current ServerWebExchange stored in Reactor Context.
     * If not found, returns the provided default value.
     *
     * @param headerName   The name of the header to retrieve.
     * @param defaultValue The default value if header is missing or exchange is unavailable.
     * @return Mono emitting the header value or the default.
     */
    public static Mono<String> getHeaderValue(String headerName, String defaultValue) {
        if (headerName == null || headerName.isBlank()) {
            return Mono.error(new IllegalArgumentException("Header name must not be null or blank"));
        }

        return Mono.deferContextual(ctxView -> {
            if (ctxView.hasKey(ExchangeContextFilter.EXCHANGE_CONTEXT_KEY)) {
                ServerWebExchange exchange = ctxView.get(ExchangeContextFilter.EXCHANGE_CONTEXT_KEY);
                HttpHeaders headers = exchange.getRequest().getHeaders();
                String headerValue = headers.getFirst(headerName);

                // Case-insensitive fallback
                if (headerValue == null) {
                    for (String name : headers.keySet()) {
                        if (name.equalsIgnoreCase(headerName)) {
                            headerValue = headers.getFirst(name);
                            break;
                        }
                    }
                }

                if (headerValue == null || headerValue.isBlank()) {
                    return Mono.just(defaultValue);
                }

                return Mono.just(headerValue.trim());
            }

            return Mono.just(defaultValue);
        });
    }
}
