package com.orange.notification_service.consumer;

import com.orange.notification_service.events.OrderPlacedEvent;
import com.orange.notification_service.service.OrderEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderPlacedConsumer {

    private final OrderEmailService orderEmailService;

    public OrderPlacedConsumer(OrderEmailService orderEmailService) {
        this.orderEmailService = orderEmailService;
    }

    @RabbitListener(queues = "order.placed.queue")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        try {
            log.info("=== NOTIFICATION SERVICE: Received OrderPlacedEvent ===");
            log.info("Order Number: {}", event.getOrderNumber());
            log.info("User ID: {}", event.getUserId());
            log.info("Total Amount: {}", event.getTotalAmount());
            log.info("Order Items Count: {}", event.getOrderItems() != null ? event.getOrderItems().size() : 0);
            
            // Send order summary email
            orderEmailService.sendOrderSummaryEmail(event);
            
            log.info("=== NOTIFICATION SERVICE: Order summary email sent successfully for order: {} ===", 
                    event.getOrderNumber());
            
        } catch (Exception e) {
            log.error("=== NOTIFICATION SERVICE: Error processing OrderPlacedEvent for order {}: {} ===", 
                    event.getOrderNumber(), e.getMessage(), e);
        }
    }
}
