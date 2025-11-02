package com.orange.gateway_service.bff.service;

import com.orange.gateway_service.bff.config.BffProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BffClient {

    private final WebClient webClient;
    private final BffProperties props;

    public Mono<Map> login(Object requestBody) {
        return webClient.post()
            .uri(props.getUserBaseUrl() + "/api/users/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .retrieve()
            .bodyToMono(Map.class);
    }

    public Mono<Map> listProducts(Map<String, String> queryParams) {
        String productBaseUrl = props.getProductBaseUrl();
        if (productBaseUrl == null || productBaseUrl.isBlank()) {
            log.error("Product base URL is not configured");
            return Mono.error(new IllegalStateException("Product base URL is not configured"));
        }
        
        // Build URI with proper encoding using UriComponentsBuilder
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromHttpUrl(productBaseUrl)
            .path("/api/products/list");
        
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach((key, value) -> {
                if (value != null) {
                    uriBuilder.queryParam(key, value);
                }
            });
        }
        
        URI uri = uriBuilder.build().toUri();
        log.info("Calling product service: {}", uri);
        
        return webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(Map.class)
            .doOnNext(response -> log.info("Product service response received. Keys: {}", 
                response != null ? response.keySet() : "null"))
            .doOnError(error -> log.error("Error calling product service: {}", error.getMessage(), error));
    }
}
