package com.orange.cart_service.cart.service;

import com.orange.cart_service.cart.config.RabbitMQConfig;
import com.orange.cart_service.cart.event.CartCheckoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class CartEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(CartEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public CartEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCartCheckoutEvent(CartCheckoutEvent event) {
        try {
            logger.info("Publishing CartCheckoutEvent to RabbitMQ: {}", event);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CART_CHECKOUT_EXCHANGE,
                    RabbitMQConfig.CART_CHECKOUT_ROUTING_KEY,
                    event
            );
            
            logger.info("Successfully published CartCheckoutEvent to RabbitMQ - Exchange: {}, RoutingKey: {}, Event: {}", 
                       RabbitMQConfig.CART_CHECKOUT_EXCHANGE, 
                       RabbitMQConfig.CART_CHECKOUT_ROUTING_KEY, 
                       event);
                       
        } catch (Exception e) {
            logger.error("Failed to publish CartCheckoutEvent to RabbitMQ: {}", event, e);
            throw new RuntimeException("Failed to publish cart checkout event", e);
        }
    }
}
