package com.orange.cart_service.client;

import com.orange.cart_service.cart.dto.ProductSummaryDto;
import com.orange.cart_service.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-service", url = "${product.service.url:http://localhost:8083}")
public interface ProductClient {

    @GetMapping("/api/products/{productId}")
    ApiResponse<ProductSummaryDto> getProductById(
            @PathVariable("productId") UUID productId
    );
}