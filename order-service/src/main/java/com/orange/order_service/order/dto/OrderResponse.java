package com.orange.order_service.order.dto;

import com.orange.order_service.order.entity.OrderStatus;
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
public class OrderResponse {
    private UUID uuid;
    private String orderNumber;
    private UUID userId;
    private UUID cartId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String shippingAddress;
    private String giftMessage;
    private List<OrderItemDto> items;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
