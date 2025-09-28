package com.orange.product_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto extends BaseDto {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Rate must be greater than or equal to 0")
    @DecimalMax(value = "5.0", inclusive = true, message = "Rate must be less than or equal to 5")
    @Digits(integer = 1, fraction = 2, message = "Rate must have at most 1 integer digit and 2 decimal places")
    private BigDecimal rate;
}
