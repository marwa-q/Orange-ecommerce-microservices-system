package com.orange.order_service.order.client;

import com.orange.order_service.common.dto.ApiResponse;
import com.orange.order_service.order.config.FeignClientConfig;
import com.orange.order_service.order.dto.CartItemResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
        name = "cart-service",
        url = "${cart.service.url:http://localhost:8084}",
        configuration = FeignClientConfig.class
)
public interface CartClient {

    @GetMapping("/api/cart/{cartId}/items")
    ApiResponse<List<CartItemResponseDto>> getCartItemsByCartId(
            @PathVariable("cartId") String cartId
            );
}
