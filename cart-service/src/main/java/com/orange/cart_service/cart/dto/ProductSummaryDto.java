package com.orange.cart_service.cart.dto;

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
public class ProductSummaryDto {

    private UUID uuid;
    private String name;
    private BigDecimal price;
    private String image;
    private Integer stock;
}


