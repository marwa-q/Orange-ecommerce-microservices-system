package com.orange.gateway_service.bff.controller;

import com.orange.gateway_service.bff.dto.product.ProductPageDto;
import com.orange.gateway_service.bff.service.AggregationService;
import com.orange.gateway_service.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping(path = "/bff/products", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProductsBffController {

    private final AggregationService aggregationService;

    @GetMapping
    public Mono<ApiResponse<ProductPageDto>> list(
        @RequestParam Map<String, String> query
    ) {
        return aggregationService.aggregateProducts(query)
            .flatMap(ApiResponse::success);
    }
}
