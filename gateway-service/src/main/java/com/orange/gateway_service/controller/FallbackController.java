package com.orange.gateway_service.controller;

import com.orange.gateway_service.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Tag(name = "Fallback Management", description = "Fallback responses when services are unavailable")
public class FallbackController {

    @GetMapping("/user-service")
    @Operation(
            summary = "User Service Fallback",
            description = "Fallback response when the user service is unavailable or experiencing issues"
    )
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> userServiceFallback(ServerWebExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "user-service");
        response.put("status", "unavailable");
        response.put("message", "User service is currently unavailable. Please try again later.");
        response.put("fallback", true);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.failure("service.unavailable", exchange);
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(apiResponse));
    }
}
