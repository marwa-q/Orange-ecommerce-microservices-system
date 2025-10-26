package com.orange.gateway_service.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.gateway_service.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ResponseWrapperFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    // Paths that should not be wrapped (actuator, swagger, etc.)
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/swagger-ui", "/v3/api-docs", "/swagger", 
        "/actuator", "/swagger-ui.html", "/swagger-ui.html"
    );

    public ResponseWrapperFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        // Skip wrapping for excluded paths
        if (shouldSkipWrapping(path)) {
            log.debug("Skipping response wrapping for path: {}", path);
            return chain.filter(exchange);
        }

        ServerHttpResponse response = exchange.getResponse();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                return DataBufferUtils.join(body)
                    .flatMap(dataBuffer -> {
                        byte[] bytes = null;
                        try {
                            bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);

                            String responseBody = new String(bytes, StandardCharsets.UTF_8);
                            log.debug("Original response body: {}", responseBody);

                            // Try to parse as JSON and wrap it
                            Object responseData = objectMapper.readValue(responseBody, Object.class);

                            // Create ApiResponse wrapper
                            String successMessage = getSuccessMessage(path);
                            ApiResponse<Object> apiResponse = ApiResponse.success(responseData, exchange);

                            // Convert back to bytes
                            String wrappedJson = objectMapper.writeValueAsString(apiResponse);
                            byte[] wrappedBytes = wrappedJson.getBytes(StandardCharsets.UTF_8);
                            
                            DataBufferFactory bufferFactory = response.bufferFactory();
                            DataBuffer buffer = bufferFactory.wrap(wrappedBytes);
                            
                            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            response.getHeaders().setContentLength(wrappedBytes.length);
                            
                            return getDelegate().writeWith(Mono.just(buffer));
                            
                        } catch (Exception e) {
                            log.error("Error wrapping response for path: {}", path, e);
                            // If wrapping fails, return original response using stored bytes
                            if (bytes != null) {
                                DataBufferFactory bufferFactory = response.bufferFactory();
                                DataBuffer originalBuffer = bufferFactory.wrap(bytes);
                                return getDelegate().writeWith(Mono.just(originalBuffer));
                            } else {
                                // If we couldn't read bytes, return empty response
                                return getDelegate().writeWith(Mono.empty());
                            }
                        }
                    });
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    private boolean shouldSkipWrapping(String path) {
        // Skip actuator endpoints
        if (path.contains("/actuator")) {
            return true;
        }
        
        // Skip swagger endpoints
        if (path.contains("/swagger-ui") || path.contains("/v3/api-docs") || path.contains("/swagger")) {
            return true;
        }
        
        // Skip excluded paths
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private String getSuccessMessage(String path) {
        // Determine success message based on path
        if (path.contains("/login")) {
            return "auth.login.success";
        } else if (path.contains("/register")) {
            return "auth.register.success";
        } else if (path.contains("/create")) {
            return "create.success";
        } else if (path.contains("/update")) {
            return "update.success";
        } else if (path.contains("/delete")) {
            return "delete.success";
        } else if (path.contains("/get") || path.contains("/list")) {
            return "fetch.success";
        }
        return "success";
    }

    @Override
    public int getOrder() {
        return -50; // Execute early, after authentication but before response
    }
}

