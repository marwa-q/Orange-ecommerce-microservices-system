package com.orange.product_service.listener;

import com.orange.product_service.event.LowStockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class LowStockEventListener {

    private static final Logger logger = LoggerFactory.getLogger(LowStockEventListener.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.low-stock}")
    public void handleLowStockEvent(LowStockEvent event) {
        logger.warn("🚨 LOW STOCK ALERT 🚨");
        logger.warn("Product: {} (ID: {})", event.productName(), event.productId());
        logger.warn("Current Stock: {} (Threshold: {})", event.currentStock(), event.threshold());
        logger.warn("Price: ${}", event.price());
        logger.warn("Category: {}", event.categoryName());
        logger.warn("Event Time: {}", event.eventTimestamp());
        logger.warn("Service: {}", event.serviceName());
        logger.warn("=====================================");
        
        // Here you could add additional logic like:
        // - Send email notifications
        // - Update inventory management systems
        // - Trigger reorder processes
        // - Send alerts to warehouse managers
    }
}
