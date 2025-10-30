package com.orange.gateway_service.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.gateway_service.dto.ApiResponse;
import com.orange.gateway_service.config.GatewayAppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final GatewayAppProperties gatewayProperties;

    // Values will be read from gatewayProperties
    private int getRequestsPerMinute() {
        return gatewayProperties.getRateLimiting() != null
            ? gatewayProperties.getRateLimiting().getRequestsPerMinute()
            : 5;
    }

    private int getRequestsPerHour() {
        return gatewayProperties.getRateLimiting() != null
            ? gatewayProperties.getRateLimiting().getRequestsPerHour()
            : 1000;
    }

    private List<String> getExcludedPaths() {
        return gatewayProperties.getRateLimiting() != null
            ? gatewayProperties.getRateLimiting().getExcludedPaths()
            : null;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip rate limiting for excluded paths
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        // Get client identifier (IP address)
        String clientId = getClientIdentifier(request);

        // Check rate limit
        return isAllowed(clientId)
            .flatMap(allowed -> {
                if (allowed) {
                    log.debug("Rate limit check passed for client: {} on path: {}", clientId, path);
                    return chain.filter(exchange);
                } else {
                    log.warn("Rate limit exceeded for client: {} on path: {}", clientId, path);
                    return tooManyRequests(exchange);
                }
            });
    }

    /**
     * Get client identifier (IP address from request)
     */
    private String getClientIdentifier(ServerHttpRequest request) {
        // Try to get IP from X-Forwarded-For header (for proxies)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // Try to get IP from X-Real-IP header
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        return request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress() 
            : "unknown";
    }

    /**
     * Check if request is allowed based on rate limiting rules
     * Uses atomic Redis operations to prevent race conditions
     */
    private Mono<Boolean> isAllowed(String clientId) {
        String minuteKey = "rate_limit:minute:" + clientId;
        String hourKey = "rate_limit:hour:" + clientId;
        
        // Use atomic increment operations
        return redisTemplate.opsForValue().increment(minuteKey)
            .defaultIfEmpty(1L)
            .flatMap(minuteCount -> {
                // Set expiry if this is the first request in this minute
                if (minuteCount == 1) {
                    redisTemplate.expire(minuteKey, Duration.ofMinutes(1)).subscribe();
                }
                
                int requestsPerMinute = getRequestsPerMinute();
                if (minuteCount > requestsPerMinute) {
                    return Mono.just(false);
                }
                
                // Check and increment hour counter
                return redisTemplate.opsForValue().increment(hourKey)
                    .defaultIfEmpty(1L)
                    .flatMap(hourCount -> {
                        // Set expiry if this is the first request in this hour
                        if (hourCount == 1) {
                            redisTemplate.expire(hourKey, Duration.ofHours(1)).subscribe();
                        }
                        
                        int requestsPerHour = getRequestsPerHour();
                        if (hourCount > requestsPerHour) {
                            return Mono.just(false);
                        }
                        
                        return Mono.just(true);
                    });
            })
            .onErrorResume(e -> {
                log.error("Error checking rate limit for client: {}", clientId, e);
                // On error, allow the request to prevent service disruption
                return Mono.just(true);
            });
    }

    /**
     * Check if path is excluded from rate limiting
     */
    private boolean isExcludedPath(String path) {
        List<String> excludedPaths = getExcludedPaths();
        return excludedPaths != null && excludedPaths.stream().anyMatch(excludedPath -> path.startsWith(excludedPath));
    }

    /**
     * Return too many requests response
     */
    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        int requestsPerMinute = getRequestsPerMinute();
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.getHeaders().add("X-RateLimit-Remaining", "0");

        // Use ApiResponse.failure() to resolve message through ApiResponse
        return ApiResponse.failure("gateway.rate.limit")
            .flatMap(apiResponse -> {
                try {
                    String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                    byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
                } catch (JsonProcessingException e) {
                    log.error("Error serializing ApiResponse", e);
                    String fallbackResponse = "{\"success\":false,\"message\":\"gateway.rate.limit\",\"data\":null}";
                    byte[] bytes = fallbackResponse.getBytes(StandardCharsets.UTF_8);
                    return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
                }
            })
            .onErrorResume(e -> {
                log.error("Error in tooManyRequests", e);
                String fallbackResponse = "{\"success\":false,\"message\":\"gateway.rate.limit\",\"data\":null}";
                byte[] bytes = fallbackResponse.getBytes(StandardCharsets.UTF_8);
                return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
            });
    }

    @Override
    public int getOrder() {
        return -200; // Execute early, before other filters
    }
}
