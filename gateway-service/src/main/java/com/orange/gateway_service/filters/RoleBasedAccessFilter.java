package com.orange.gateway_service.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.gateway_service.dto.ApiResponse;
import com.orange.gateway_service.config.GatewayAppProperties;
import com.orange.gateway_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RoleBasedAccessFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final GatewayAppProperties gatewayProperties;

    public RoleBasedAccessFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, GatewayAppProperties gatewayProperties) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.gatewayProperties = gatewayProperties;
    }

    // Role-based paths now come from configuration via GatewayAppProperties

    // Public paths are read from configuration (gateway.public-paths)

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Allow public paths
        if (isPublicPath(path)) {
            log.debug("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        // Extract token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return unauthorized(exchange, "auth.missing.token");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Validate token
            if (!jwtUtil.validate(token)) {
                log.warn("Invalid JWT token for path: {}", path);
                return unauthorized(exchange, "auth.invalid.token");
            }

            // Extract user role from token
            String role = jwtUtil.getRole(token);
            if (role == null) {
                log.warn("No role found in token for path: {}", path);
                return forbidden(exchange, "auth.missing.role");
            }

            // Check if user has access to the path
            if (!hasAccess(role, path)) {
                log.warn("Access denied for role '{}' to path: {}", role, path);
                return forbidden(exchange, "auth.access.denied");
            }

            log.debug("Access granted for role '{}' to path: {}", role, path);
            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("Error processing JWT token for path: {}", path, e);
            return unauthorized(exchange, "auth.token.error");
        }
    }

    

    private boolean isPublicPath(String path) {
        List<String> publicPaths = gatewayProperties.getPublicPaths();
        return publicPaths != null && publicPaths.stream().anyMatch(publicPath ->
            path.startsWith(publicPath) ||
            path.equals("/") ||
            path.matches("/swagger.*")
        );
    }

    private boolean hasAccess(String userRole, String path) {
        // Admin has access to everything
        if ("ADMIN".equals(userRole)) {
            return true;
        }

        // If path is admin-only, block non-admin roles
        Map<String, List<String>> roleBasedPaths = gatewayProperties.getRoleBasedPaths();
        List<String> adminPaths = roleBasedPaths != null ? roleBasedPaths.get("ADMIN") : null;
        if (adminPaths != null && adminPaths.stream().anyMatch(path::startsWith)) {
            return false;
        }

        // Check if path is in user's allowed paths
        List<String> allowedPaths = roleBasedPaths != null ? roleBasedPaths.get(userRole.toUpperCase()) : null;
        if (allowedPaths != null) {
            return allowedPaths.stream().anyMatch(allowedPath -> path.startsWith(allowedPath));
        }

        // Check if path is public
        List<String> publicPathsFromRoleMap = roleBasedPaths != null ? roleBasedPaths.get("PUBLIC") : null;
        if (publicPathsFromRoleMap != null) {
            return publicPathsFromRoleMap.stream().anyMatch(publicPath -> path.startsWith(publicPath));
        }

        // Deny access by default
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String messageKey) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return writeApiResponse(response, messageKey, HttpStatus.UNAUTHORIZED);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String messageKey) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return writeApiResponse(response, messageKey, HttpStatus.FORBIDDEN);
    }

    private Mono<Void> writeApiResponse(ServerHttpResponse response, String messageKey, HttpStatus status) {
        return ApiResponse.failure(messageKey)
            .flatMap(apiResponse -> {
                try {
                    String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                    byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
                } catch (JsonProcessingException e) {
                    String fallbackResponse = "{\"success\":false,\"message\":\"" + messageKey + "\",\"data\":null}";
                    byte[] bytes = fallbackResponse.getBytes(StandardCharsets.UTF_8);
                    return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
                }
            })
            .onErrorResume(e -> {
                String fallbackResponse = "{\"success\":false,\"message\":\"" + messageKey + "\",\"data\":null}";
                byte[] bytes = fallbackResponse.getBytes(StandardCharsets.UTF_8);
                return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
            });
    }

    @Override
    public int getOrder() {
        return -90; // Execute after JwtAuthenticationFilter (-100) but before other filters
    }
}