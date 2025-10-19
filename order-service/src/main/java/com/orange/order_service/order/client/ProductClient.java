package com.orange.order_service.order.client;

import com.orange.order_service.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "product-service",
        url = "${product.service.url:http://localhost:8083}",
        configuration = com.orange.order_service.order.config.FeignClientConfig.class
)
public interface ProductClient {

    @GetMapping("/api/products/{id}/name")
    ApiResponse<String> getProductNameById(@PathVariable("id") UUID productId);

    @PostMapping("/api/products/{id}/stock")
    ApiResponse<Void> setStock(
            @PathVariable("id") UUID productId,
            @RequestParam("quantity") int quantity,
            @RequestParam("action") String action
    );
}