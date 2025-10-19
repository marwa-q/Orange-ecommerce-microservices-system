package com.orange.notification_service.client;

import com.orange.notification_service.config.FeignClientConfig;
import com.orange.notification_service.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8081",
        configuration = FeignClientConfig.class
)
public interface UserClient {
    @GetMapping("/api/users/{userId}/email")
    ApiResponse<String> getUserEmailById(@PathVariable("userId") String userId);
}