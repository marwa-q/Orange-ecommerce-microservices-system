package com.orange.gateway_service.bff.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    private String variantId;
    private String name;
    private Map<String, String> attributes; // e.g., {"color": "red", "size": "M"}
    private BigDecimal price;
    private Integer stock;
    private String sku;
    private String image;
    private Boolean isActive;
}
