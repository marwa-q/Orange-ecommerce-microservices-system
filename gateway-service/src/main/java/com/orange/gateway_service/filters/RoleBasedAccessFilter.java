package com.orange.gateway_service.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.gateway_service.dto.ApiResponse;
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
@RequiredArgsConstructor
public class RoleBasedAccessFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // Define role-based access control rules
    private static final Map<String, List<String>> ROLE_BASED_PATHS = new HashMap<>();

    static {
        // Admin-only paths
        ROLE_BASED_PATHS.put("ADMIN", Arrays.asList(
            "/api/admin",
            "/api/users/admin",
            "/api/orders/admin",
            "/api/products/admin",
            "/api/cart/admin",
            "/api/notifications/admin"
        ));

        // User paths (authenticated users)
        ROLE_BASED_PATHS.put("USER", Arrays.asList(
            "/api/users/me",
            "/api/users/update",
            "/api/orders",
            "/api/cart",
            "/api/products/review"
        ));

        // Public paths (no authentication required)
        ROLE_BASED_PATHS.put("PUBLIC", Arrays.asList(
            "/api/users/auth/login",
            "/api/users/auth/register",
            "/api/products/list",
            "/api/categories",
            "/api/reviews",
            "/api/tags",
            "/api/health",
            "/api/test",
            "/api/auth-test",
            "/swagger",
            "/v3/api-docs",
            "/actuator",
            "/webjars",
            "/favicon.ico"
        ));
    }

    // Define public paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/swagger-ui", "/swagger-ui.html", "/v3/api-docs", "/swagger",
        "/swagger-user", "/swagger-product", "/swagger-cart", "/swagger-order", "/swagger-notification",
        "/actuator/health", "/actuator/info",
        "/api/users/auth/login", "/api/users/auth/register",
        "/api/varify/email", "/api/varify/request-otp",
        "/api/products/list", "/api/categories", "/api/reviews", "/api/tags",
        "/api/health", "/api/test", "/api/auth-test",
        "/api/cart/actuator", "/api/orders/actuator"
    );

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
        return PUBLIC_PATHS.stream().anyMatch(publicPath ->
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

        // Check if path is in user's allowed paths
        List<String> allowedPaths = ROLE_BASED_PATHS.get(userRole.toUpperCase());
        if (allowedPaths != null) {
            return allowedPaths.stream().anyMatch(allowedPath -> path.startsWith(allowedPath));
        }

        // Check if path is public
        List<String> publicPaths = ROLE_BASED_PATHS.get("PUBLIC");
        if (publicPaths != null) {
            return publicPaths.stream().anyMatch(publicPath -> path.startsWith(publicPath));
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
        try {
            ApiResponse<Void> apiResponse = ApiResponse.error(messageKey, null); // No exchange available here
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            String fallbackResponse = "{\"success\":false,\"message\":\"Access denied\",\"data\":null}";
            byte[] bytes = fallbackResponse.getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }
    }

    @Override
    public int getOrder() {
        return -90; // Execute after JwtAuthenticationFilter (-100) but before other filters
    }
}