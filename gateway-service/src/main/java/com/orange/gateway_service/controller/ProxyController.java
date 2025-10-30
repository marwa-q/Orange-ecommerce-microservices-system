package com.orange.gateway_service.controller;

import com.orange.gateway_service.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/proxy")
@Tag(name = "Proxy Management", description = "Gateway proxy status and management operations")
public class ProxyController {

    @GetMapping("/status")
    @Operation(
            summary = "Get Proxy Status",
            description = "Returns the current status and configuration of the reverse proxy"
    )
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getProxyStatus(ServerWebExchange exchange) {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "gateway-service");
        status.put("type", "reverse-proxy");
        status.put("status", "active");
        status.put("routes", new String[]{
            "/api/users/** -> user-service (localhost:8081)",
            "/api/auth/** -> user-service (localhost:8081)",
            "/swagger-ui/** -> user-service (localhost:8081)",
            "/v3/api-docs/** -> user-service (localhost:8081)"
        });
        status.put("features", new String[]{
            "JWT Authentication",
            "Path Rewriting",
            "Retry Mechanism",
            "Request Headers",
            "CORS Support"
        });

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>(true, "Gateway proxy is active", status);

        return Mono.just(ResponseEntity.ok(apiResponse));
    }
}
