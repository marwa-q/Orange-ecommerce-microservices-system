package com.orange.notification_service.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
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
    private String shippingAddress;
    private String giftMessage;
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
