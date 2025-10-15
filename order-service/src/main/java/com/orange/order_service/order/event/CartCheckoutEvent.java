package com.orange.order_service.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartCheckoutEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private UUID cartId;
    private UUID userId;
    private BigDecimal totalAmount;
    
    @Override
    public String toString() {
        return "CartCheckoutEvent{" +
                "cartId=" + cartId +
                ", userId=" + userId +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
