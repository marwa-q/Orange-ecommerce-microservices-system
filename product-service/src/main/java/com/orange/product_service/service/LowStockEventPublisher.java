package com.orange.product_service.service;

import com.orange.product_service.event.LowStockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LowStockEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(LowStockEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.low-stock}")
    private String lowStockExchange;

    @Value("${app.rabbitmq.routing-key.low-stock}")
    private String lowStockRoutingKey;

    public LowStockEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishLowStockEvent(LowStockEvent event) {
        try {
            logger.info("Publishing low stock event for product: {} (Stock: {})", 
                       event.productName(), event.currentStock());
            
            rabbitTemplate.convertAndSend(lowStockExchange, lowStockRoutingKey, event);
            
            logger.info("Low stock event published successfully for product: {}", event.productName());
        } catch (Exception e) {
            logger.error("Failed to publish low stock event for product: {} - Error: {}", 
                        event.productName(), e.getMessage(), e);
        }
    }
}
