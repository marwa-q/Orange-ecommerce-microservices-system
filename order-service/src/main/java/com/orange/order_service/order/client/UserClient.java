package com.orange.order_service.order.client;

import com.orange.order_service.common.dto.ApiResponse;
import com.orange.order_service.order.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        url = "${user.service.url:http://localhost:8081}",
        configuration = FeignClientConfig.class
)
public interface UserClient {

    @GetMapping("/api/{userId}/email")
    ApiResponse<String> getUserEmailById(@PathVariable("userId") UUID userId);
}
