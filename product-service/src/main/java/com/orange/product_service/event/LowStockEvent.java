package com.orange.product_service.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LowStockEvent(
        UUID productId,
        String productName,
        Integer currentStock,
        Integer threshold,
        BigDecimal price,
        String categoryName,
        LocalDateTime eventTimestamp,
        String serviceName
) {
    public LowStockEvent(UUID productId, String productName, Integer currentStock, Integer threshold, 
                        BigDecimal price, String categoryName) {
        this(productId, productName, currentStock, threshold, price, categoryName, 
             LocalDateTime.now(), "product-service");
    }
}
