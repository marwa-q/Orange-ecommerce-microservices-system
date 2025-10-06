package com.orange.product_service.controller;

import com.orange.product_service.dto.ApiResponse;
import com.orange.product_service.event.LowStockEvent;
import com.orange.product_service.service.LowStockEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test Controller", description = "Test endpoints for development")
public class LowStockTestController {

    private final LowStockEventPublisher lowStockEventPublisher;

    public LowStockTestController(LowStockEventPublisher lowStockEventPublisher) {
        this.lowStockEventPublisher = lowStockEventPublisher;
    }

    @PostMapping("/low-stock-event")
    @Operation(summary = "Trigger low stock event", description = "Manually trigger a low stock event for testing")
    public ResponseEntity<ApiResponse<Void>> triggerLowStockEvent(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        Locale locale = Locale.forLanguageTag(language);
        
        // Create a test low stock event
        LowStockEvent testEvent = new LowStockEvent(
            UUID.randomUUID(),
            "Test Product",
            5, // Low stock
            10, // Threshold
            BigDecimal.valueOf(29.99),
            "Test Category"
        );
        
        lowStockEventPublisher.publishLowStockEvent(testEvent);
        
        String msg = "Low stock event triggered successfully for testing";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }
}
