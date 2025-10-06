package com.orange.product_service.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private UUID uuid;
    private UUID userId;
    private UUID productId;
    private String productName;
    private BigDecimal rate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
