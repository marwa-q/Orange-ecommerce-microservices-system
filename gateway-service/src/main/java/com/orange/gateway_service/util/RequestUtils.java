package com.orange.gateway_service.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

public class RequestUtils {


    public static String getHeaderValue(ServerWebExchange exchange, String headerName, String defaultValue) {
        if (exchange == null || headerName == null || headerName.isBlank()) {
            return defaultValue;
        }

        try {
            ServerHttpRequest request = exchange.getRequest();
            String headerValue = request.getHeaders().getFirst(headerName);

            if (headerValue == null) {
                // case-insensitive fallback
                for (String name : request.getHeaders().keySet()) {
                    if (name.equalsIgnoreCase(headerName)) {
                        headerValue = request.getHeaders().getFirst(name);
                        break;
                    }
                }
            }

            if (headerValue == null || headerValue.isBlank()) {
                return defaultValue;
            }

            return headerValue.trim();

        } catch (Exception e) {
            return defaultValue;
        }
    }
}
