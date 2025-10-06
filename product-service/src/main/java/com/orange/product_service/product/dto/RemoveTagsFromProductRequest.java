package com.orange.product_service.product.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record RemoveTagsFromProductRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,
        
        @NotEmpty(message = "At least one tag ID is required")
        List<UUID> tagIds
) {
}
