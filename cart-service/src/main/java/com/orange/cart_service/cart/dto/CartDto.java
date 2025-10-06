package com.orange.cart_service.cart.dto;

import com.orange.cart_service.cart.enums.CartStatus;
import com.orange.cart_service.cartItem.dto.CartItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private UUID uuid;
    private UUID userId;
    private CartStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiredAt;
    private List<CartItemDto> cartItems;
}
