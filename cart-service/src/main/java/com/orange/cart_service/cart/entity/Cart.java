package com.orange.cart_service.cart.entity;

import com.orange.cart_service.cart.enums.CartStatus;
import com.orange.cart_service.cartItem.entity.CartItem;
import com.orange.cart_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CartStatus status = CartStatus.ACTIVE;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        this.expiredAt = LocalDateTime.now().plusHours(24);
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        this.expiredAt = LocalDateTime.now().plusHours(24);
    }

    // Helper methods
    public void addCartItem(CartItem cartItem) {
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        cartItems.add(cartItem);
        cartItem.setCart(this);
        calculateTotalAmount();
    }

    public void removeCartItem(CartItem cartItem) {
        if (cartItems != null) {
            cartItems.remove(cartItem);
            cartItem.setCart(null);
            calculateTotalAmount();
        }
    }

    public void calculateTotalAmount() {
        if (cartItems == null || cartItems.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
        } else {
            this.totalAmount = cartItems.stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }

    public void markAsExpired() {
        this.status = CartStatus.EXPIRED;
    }

    public void markAsCheckedOut() {
        this.status = CartStatus.CHECKED_OUT;
    }
}
