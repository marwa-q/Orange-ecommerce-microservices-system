package com.orange.cart_service.cartItem.entity;

import com.orange.cart_service.cart.entity.Cart;
import com.orange.cart_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 3)
    private BigDecimal price;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Helper methods
    public void calculateSubtotal() {
        if (price != null && quantity != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateSubtotal();
        // Update cart total when quantity changes
        if (cart != null) {
            cart.calculateTotalAmount();
        }
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        calculateSubtotal();
        // Update cart total when price changes
        if (cart != null) {
            cart.calculateTotalAmount();
        }
    }
}