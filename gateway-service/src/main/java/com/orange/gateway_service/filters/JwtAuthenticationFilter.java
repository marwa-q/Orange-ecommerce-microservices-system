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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // Define public paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/swagger-ui", "/swagger-ui.html", "/v3/api-docs", "/swagger",
        "/swagger-user", "/swagger-product", "/swagger-cart", "/swagger-order", "/swagger-notification",
        "/actuator/health", "/actuator/info",
        "/api/users/auth/login", "/api/users/auth/register",
        "/api/varify/email", "/api/varify/request-otp",
        "/api/products/list", "/api/categories", "/api/reviews", "/api/tags",
        "/api/health", "/api/test", "/api/auth-test",
        "/api/cart/actuator", "/api/orders/actuator"  // Allow actuator endpoints
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
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Validate token
            if (!jwtUtil.validate(token)) {
                log.warn("Invalid JWT token for path: {}", path);
                return unauthorized(exchange);
            }

            // Extract user information from token
            String email = jwtUtil.getEmail(token);
            String role = jwtUtil.getRole(token);
            String userId = jwtUtil.getUserId(token) != null ? jwtUtil.getUserId(token).toString() : null;

            log.debug("Authenticated user: {} with role: {} for path: {}", email, role, path);

            // Create authentication object
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null,
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_" + role)));

            // Add user info to request headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

            // Set security context
            SecurityContext context = new SecurityContextImpl(authentication);

            return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));

        } catch (Exception e) {
            log.error("Error processing JWT token for path: {}", path, e);
            return unauthorized(exchange);
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(publicPath ->
            path.startsWith(publicPath) ||
            path.equals("/") ||
            path.matches("/swagger.*")
        );
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Create ApiResponse for unauthorized
        ApiResponse<Void> apiResponse = ApiResponse.error("auth.unauthorized", exchange);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            String fallbackResponse = "{\"success\":false,\"message\":\"Unauthorized\",\"data\":null}";
            byte[] bytes = fallbackResponse.getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }
    }

    @Override
    public int getOrder() {
        return -100; // Execute very early, before route-specific filters
    }
}
