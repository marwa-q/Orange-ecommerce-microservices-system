package com.orange.order_service.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    
    private UUID orderId;
    private String orderNumber;
    private UUID userId;
    private UUID cartId;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String giftMessage;
    private String shippingAddress;
    private LocalDateTime orderDate;
    private String status;
    
    // Additional fields for email notification
    private String userEmail;
    
    // Order items information
    private List<OrderItemInfo> orderItems;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private UUID productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
        private String productImage;
    }
}
