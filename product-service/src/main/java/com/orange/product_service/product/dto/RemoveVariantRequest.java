package com.orange.product_service.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RemoveVariantRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,
        
        @NotBlank(message = "Variant ID is required")
        String variantId
) {
}
