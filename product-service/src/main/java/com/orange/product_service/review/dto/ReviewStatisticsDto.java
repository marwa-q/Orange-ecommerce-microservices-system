package com.orange.product_service.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatisticsDto {
    private UUID productId;
    private Long reviewCount;
    private BigDecimal averageRating;
    private BigDecimal minRating;
    private BigDecimal maxRating;
}
