package com.orange.gateway_service.advice;

import com.orange.gateway_service.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(ConnectException.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleConnectException(ConnectException ex) {
        return ApiResponse.failure("gateway.service.unavailable")
            .map(body -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }


    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());

        if (status == HttpStatus.FORBIDDEN) {
            log.info("Access denied: {}", ex.getReason());
            return ApiResponse.failure("access.denied")
                    .map(body -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(body));
        }

        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            log.info("Gateway timeout: {}", ex.getReason());
            return ApiResponse.failure("gateway.timeout")
                    .map(body -> ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(body));
        }

        if (status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.BAD_GATEWAY) {
            log.info("Service unavailable via ResponseStatusException: {}", ex.getReason());
            return ApiResponse.failure("gateway.service.unavailable")
                    .map(body -> ResponseEntity.status(status).body(body));
        }

        // For other status codes, use the exception's status
        log.warn("ResponseStatusException with status {}: {}", status, ex.getReason());
        return ApiResponse.failure("error.generic")
                .map(body -> ResponseEntity.status(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleAllExceptions(Exception ex, ServerHttpRequest request) {
        log.warn("Unhandled exception: {}", ex.toString());
        return ApiResponse.failure("error.generic")
            .map(body -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }
}
