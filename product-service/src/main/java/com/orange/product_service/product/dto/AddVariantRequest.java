package com.orange.product_service.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record AddVariantRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,
        
        @NotBlank(message = "Variant name is required")
        String name,
        
        @NotNull(message = "Variant attributes are required")
        Map<String, String> attributes,
        
        @NotNull(message = "Variant price is required")
        BigDecimal price,
        
        @NotNull(message = "Variant stock is required")
        Integer stock,
        
        String sku,
        String image,
        Boolean isActive
) {
}
