package com.orange.order_service.order.entity;

import com.orange.order_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 3)
    private BigDecimal totalAmount;

    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod = PaymentMethod.CACHE_ON_DELIVERY;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "gift_message", columnDefinition = "TEXT")
    private String giftMessage;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        
        // Generate order number if not already set
        if (orderNumber == null || orderNumber.isEmpty()) {
            orderNumber = generateOrderNumber();
        }
    }

    public void markAsConfirmed() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void markAsUnderReview() {
        this.status = OrderStatus.UNDER_REVIEW;
    }

    public void markAsShipped() {
        this.status = OrderStatus.SHIPPED;
    }

    public void markAsDeliveredAndSetTime() {
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();;

    }
    
    /**
     * Generates a unique order number in the format: ORD-YYYYMMDD-HHMMSS-XXXX
     */
    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();

        // Force English locale to ensure Western digits (0–9)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss", Locale.ENGLISH);

        String dateStr = now.format(dateFormatter);
        String timeStr = now.format(timeFormatter);

        // Use nanoseconds for uniqueness (last 4 digits)
        long nanoSeconds = now.getNano();
        String nanoStr = String.format(Locale.ENGLISH, "%04d", (nanoSeconds / 100000) % 10000);

        return String.format(Locale.ENGLISH, "ORD-%s-%s-%s", dateStr, timeStr, nanoStr);
    }

}
