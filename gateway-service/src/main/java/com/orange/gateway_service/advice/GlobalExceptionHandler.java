package com.orange.gateway_service.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.gateway_service.dto.ApiResponse;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-1000)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorKey = "internal.error";
        Map<String, Object> errorDetails = new HashMap<>();
        
        // Determine status and error key based on exception type
        if (ex instanceof WebClientResponseException) {
            WebClientResponseException webClientEx = (WebClientResponseException) ex;
            status = HttpStatus.valueOf(webClientEx.getStatusCode().value());
            errorKey = "service.error";
            errorDetails.put("status", status.value());
            errorDetails.put("originalMessage", webClientEx.getMessage());
        } else if (ex instanceof WebClientException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorKey = "service.unavailable";
            errorDetails.put("originalMessage", ex.getMessage());
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusEx = (ResponseStatusException) ex;
            status = (HttpStatus) responseStatusEx.getStatusCode();
            errorKey = "service.error";
            errorDetails.put("originalMessage", responseStatusEx.getMessage());
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            errorKey = "validation.error";
            errorDetails.put("originalMessage", ex.getMessage());
        }
        
        // Add common error details
        errorDetails.put("path", exchange.getRequest().getPath().value());
        errorDetails.put("timestamp", System.currentTimeMillis());
        errorDetails.put("exception", ex.getClass().getSimpleName());

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Create ApiResponse with localized message
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.error(errorKey, exchange);
        apiResponse.setData(errorDetails);

        try {
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            DataBufferFactory bufferFactory = response.bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            // Simple fallback response if JSON serialization fails
            String fallbackResponse = "{\"success\":false,\"message\":\"" + errorKey + "\",\"data\":null}";
            DataBufferFactory bufferFactory = response.bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(fallbackResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }
}
