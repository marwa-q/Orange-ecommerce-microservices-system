package com.orange.order_service.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotNull(message = "Cart ID is required")
    private UUID cartId;
    
    @Valid
    @NotNull(message = "Order items are required")
    private List<OrderItemDto> items;
    
    private BigDecimal totalAmount;
    
    private String paymentMethod;
    
    private String shippingAddress;
    
}
