package com.orange.product_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto extends BaseDto {
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String image;
    
    @Min(value = 0, message = "Stock must be greater than or equal to 0")
    private Integer stock = 0;
    
    private Boolean isDeleted = false;
    private Long viewCount = 0L;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Rate must be greater than or equal to 0")
    @DecimalMax(value = "5.0", inclusive = true, message = "Rate must be less than or equal to 5")
    @Digits(integer = 1, fraction = 2, message = "Rate must have at most 1 integer digit and 2 decimal places")
    private BigDecimal rate = BigDecimal.ZERO;
    
    @NotBlank(message = "Category ID is required")
    private String categoryId;
    private String categoryName;
    
    private List<TagDto> tags;
    private List<ReviewDto> reviews;
}