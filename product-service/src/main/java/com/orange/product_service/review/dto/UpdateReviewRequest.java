package com.orange.product_service.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateReviewRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,
        
        @NotNull(message = "Rating is required")
        @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
        @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
        BigDecimal rate
) {
}
