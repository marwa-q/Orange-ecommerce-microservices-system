package com.orange.gateway_service.bff.controller;

import com.orange.gateway_service.dto.ApiResponse;
import com.orange.gateway_service.bff.service.BffClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping(path = "/bff/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthBffController {

    private final BffClient client;

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ApiResponse<Map>> login(@RequestBody Map<String, Object> body) {
        return client.login(body)
            .flatMap(ApiResponse::success);
    }
}
