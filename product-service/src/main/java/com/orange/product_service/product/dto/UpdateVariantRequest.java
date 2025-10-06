package com.orange.product_service.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record UpdateVariantRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,
        
        @NotBlank(message = "Variant ID is required")
        String variantId,
        
        String name,
        Map<String, String> attributes,
        BigDecimal price,
        Integer stock,
        String sku,
        String image,
        Boolean isActive
) {
}
