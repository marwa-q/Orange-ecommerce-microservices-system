package com.orange.gateway_service.bff.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAggregateDto {
    private String id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private Double averageRating;
    private Integer reviewsCount;
    private List<String> tags;
}





