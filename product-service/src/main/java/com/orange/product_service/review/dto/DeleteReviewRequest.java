package com.orange.product_service.review.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteReviewRequest(
        @NotNull(message = "Review ID is required")
        UUID reviewId
) {
}
