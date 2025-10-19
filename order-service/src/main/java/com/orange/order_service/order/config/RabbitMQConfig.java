package com.orange.order_service.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RabbitMQConfig {

    // Queue names
    public static final String CART_CHECKOUT_QUEUE = "cart.checkout.queue";
    public static final String ORDER_PLACED_QUEUE = "order.placed.queue";
    
    // Exchange names
    public static final String CART_EXCHANGE = "cart.checkout.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    
    // Routing keys
    public static final String CART_CHECKOUT_ROUTING_KEY = "cart.checkout";
    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    // Cart Exchange
    @Bean
    public DirectExchange cartExchange() {
        log.info("Creating DirectExchange: {}", CART_EXCHANGE);
        return new DirectExchange(CART_EXCHANGE);
    }

    // Cart Checkout Queue
    @Bean
    public Queue cartCheckoutQueue() {
        log.info("Creating Queue: {}", CART_CHECKOUT_QUEUE);
        return QueueBuilder.durable(CART_CHECKOUT_QUEUE).build();
    }

    // Binding for cart checkout
    @Bean
    public Binding cartCheckoutBinding() {
        log.info("Creating Binding: Queue={} -> Exchange={} with RoutingKey={}", 
                CART_CHECKOUT_QUEUE, CART_EXCHANGE, CART_CHECKOUT_ROUTING_KEY);
        return BindingBuilder
                .bind(cartCheckoutQueue())
                .to(cartExchange())
                .with(CART_CHECKOUT_ROUTING_KEY);
    }

    // Order Exchange
    @Bean
    public DirectExchange orderExchange() {
        log.info("Creating DirectExchange: {}", ORDER_EXCHANGE);
        return new DirectExchange(ORDER_EXCHANGE);
    }

    // Order Placed Queue
    @Bean
    public Queue orderPlacedQueue() {
        log.info("Creating Queue: {}", ORDER_PLACED_QUEUE);
        return QueueBuilder.durable(ORDER_PLACED_QUEUE).build();
    }

    // Binding for order placed
    @Bean
    public Binding orderPlacedBinding() {
        log.info("Creating Binding: Queue={} -> Exchange={} with RoutingKey={}", 
                ORDER_PLACED_QUEUE, ORDER_EXCHANGE, ORDER_PLACED_ROUTING_KEY);
        return BindingBuilder
                .bind(orderPlacedQueue())
                .to(orderExchange())
                .with(ORDER_PLACED_ROUTING_KEY);
    }
}
