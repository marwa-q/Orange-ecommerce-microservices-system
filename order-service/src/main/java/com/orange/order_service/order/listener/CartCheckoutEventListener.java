package com.orange.order_service.order.listener;

import com.orange.order_service.common.dto.ApiResponse;
import com.orange.order_service.order.dto.OrderResponse;
import com.orange.order_service.order.event.CartCheckoutEvent;
import com.orange.order_service.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CartCheckoutEventListener {

    private final OrderService orderService;

    public CartCheckoutEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "cart.checkout.queue")
    public void handleCartCheckout(CartCheckoutEvent event) {
        try {
            log.info("=== ORDER SERVICE: Received cart checkout event ===");
            log.info("Event details: {}", event);
            log.info("Cart ID: {}", event.getCartId());
            log.info("User ID: {}", event.getUserId());
            log.info("Total Amount: {}", event.getTotalAmount());
            
            // Create order from cart checkout event
            ApiResponse<OrderResponse> response = orderService.createOrderFromCartCheckout(
                    event.getCartId(), 
                    event.getUserId(), 
                    event.getTotalAmount()
            );
            
            if (response.isSuccess()) {
                log.info("=== ORDER SERVICE: Order created successfully from cart checkout for cart: {} ===", event.getCartId());
            } else {
                log.error("=== ORDER SERVICE: Failed to create order from cart checkout for cart: {}. Error: {} ===", 
                         event.getCartId(), response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("=== ORDER SERVICE: Error processing cart checkout event: {} ===", e.getMessage(), e);
        }
    }
}
