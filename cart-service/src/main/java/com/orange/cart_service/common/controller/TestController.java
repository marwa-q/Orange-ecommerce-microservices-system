package com.orange.cart_service.common.controller;


import com.orange.cart_service.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test Controller", description = "Testing endpoints")
public class TestController {


    @GetMapping("/test")
    @Operation(summary = "Test API Response with Messages",
            description = "Test endpoint to verify ApiResponse with different message keys and languages")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testApiResponse(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

        Map<String, Object> testData = new HashMap<>();
        testData.put("service", "Cart Service");
        testData.put("language", language);
        testData.put("timestamp", System.currentTimeMillis());
        testData.put("message", "This is a test endpoint for ApiResponse with message keys");

        // Test different message keys
        return ResponseEntity.ok(ApiResponse.success("test.message", testData));
    }

    @GetMapping("/test/success")
    @Operation(summary = "Test Success Response",
            description = "Test success response with default message")
    public ResponseEntity<ApiResponse<String>> testSuccess() {
        return ResponseEntity.ok(ApiResponse.success("cart.created"));
    }

    @GetMapping("/test/error")
    @Operation(summary = "Test Error Response",
            description = "Test error response with message key")
    public ResponseEntity<ApiResponse<String>> testError() {
        return ResponseEntity.badRequest().body(ApiResponse.error("cart.not.found"));
    }

    @GetMapping("/test/failure")
    @Operation(summary = "Test Failure Response",
            description = "Test failure response with message key")
    public ResponseEntity<ApiResponse<String>> testFailure() {
        return ResponseEntity.badRequest().body(ApiResponse.failure("error.validation"));
    }

    @GetMapping("/test/multiple")
    @Operation(summary = "Test Multiple Message Keys",
            description = "Test multiple message keys in a single response")
    public ResponseEntity<ApiResponse<Map<String, String>>> testMultipleMessages() {
        Map<String, String> messages = new HashMap<>();
        messages.put("success", "success");
        messages.put("cart_created", "cart.created");
        messages.put("cart_item_added", "cart.item.added");
        messages.put("error_general", "error.general");

        return ResponseEntity.ok(ApiResponse.success("test.message", messages));
    }

    @GetMapping("/test/arabic")
    @Operation(summary = "Test Arabic Messages",
            description = "Test Arabic message resolution")
    public ResponseEntity<ApiResponse<Map<String, String>>> testArabicMessages() {
        Map<String, String> arabicMessages = new HashMap<>();
        arabicMessages.put("cart_created_ar", "cart.created");
        arabicMessages.put("cart_item_added_ar", "cart.item.added");
        arabicMessages.put("success_ar", "success");

        return ResponseEntity.ok(ApiResponse.success("test.message", arabicMessages));
    }
}
