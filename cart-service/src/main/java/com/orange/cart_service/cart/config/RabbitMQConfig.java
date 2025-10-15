package com.orange.cart_service.cart.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.cart.checkout.queue}")
    public static final String CART_CHECKOUT_QUEUE = "cart.checkout.queue";

    @Value("${rabbitmq.cart.checkout.exchange}")
    public static final String CART_CHECKOUT_EXCHANGE = "cart.checkout.exchange";

    @Value("${rabbitmq.cart.checkout.routing-key}")
    public static final String CART_CHECKOUT_ROUTING_KEY = "cart.checkout";


    @Bean
    public Queue cartCheckoutQueue() {
        return new Queue(CART_CHECKOUT_QUEUE, true);
    }

    @Bean
    public DirectExchange cartCheckoutExchange() {
        return new DirectExchange(CART_CHECKOUT_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
