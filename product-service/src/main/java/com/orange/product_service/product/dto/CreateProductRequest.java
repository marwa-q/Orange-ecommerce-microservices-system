package com.orange.product_service.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateProductRequest(
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    String name,
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    BigDecimal price,
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    String image,
    
    @Min(value = 0, message = "Stock must be greater than or equal to 0")
    Integer stock,
    
    @NotNull(message = "Category ID is required")
    UUID categoryId
) {
}
