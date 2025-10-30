package com.orange.order_service.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/orders/test-delay")
    public ResponseEntity<String> testDelay() {
        try {
            // Simulate a slow response (5 seconds) - will timeout if gateway timeout < 5s
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return ResponseEntity.ok("Response processed after delay");
    }

    @GetMapping("/api/orders/test-delay-short")
    public ResponseEntity<String> testDelayShort() {
        try {
            // Simulate a short delay (2 seconds) - should complete within timeout
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return ResponseEntity.ok("Response processed after short delay");
    }

}
