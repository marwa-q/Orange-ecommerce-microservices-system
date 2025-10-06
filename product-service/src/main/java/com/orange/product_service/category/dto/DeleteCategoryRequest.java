package com.orange.product_service.category.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteCategoryRequest(
        @NotNull(message = "Category ID is required")
        UUID categoryId
) {
}
