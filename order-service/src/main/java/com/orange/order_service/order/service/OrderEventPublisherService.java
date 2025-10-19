package com.orange.order_service.order.service;

import com.orange.order_service.common.dto.ApiResponse;
import com.orange.order_service.order.client.ProductClient;
import com.orange.order_service.order.client.UserClient;
import com.orange.order_service.order.config.RabbitMQConfig;
import com.orange.order_service.order.dto.CartItemResponseDto;
import com.orange.order_service.order.dto.OrderItemDto;
import com.orange.order_service.order.entity.Order;
import com.orange.order_service.order.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderEventPublisherService {

    private final RabbitTemplate rabbitTemplate;
    private final OrderItemService orderItemService;
    private final UserClient userClient;
    private final ProductClient productClient;

    public OrderEventPublisherService(RabbitTemplate rabbitTemplate, OrderItemService orderItemService, UserClient userClient, ProductClient productClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderItemService = orderItemService;
        this.userClient = userClient;
        this.productClient = productClient;
    }

    /**
     * Publish OrderPlacedEvent when an order is submitted
     * This event will be consumed by the notification service to send order summary email
     */
    public void publishOrderPlacedEvent(Order order) {
        try {
            log.info("Publishing OrderPlacedEvent for order: {}", order.getOrderNumber());

            // Create OrderPlacedEvent
            OrderPlacedEvent event = new OrderPlacedEvent();
            event.setOrderId(order.getUuid());
            event.setOrderNumber(order.getOrderNumber());
            event.setUserId(order.getUserId());
            event.setCartId(order.getCartId());
            event.setTotalAmount(order.getTotalAmount());
            event.setPaymentMethod(order.getPaymentMethod().toString());
            event.setShippingAddress(order.getShippingAddress());
            event.setGiftMessage(order.getGiftMessage());
            event.setOrderDate(LocalDateTime.now());
            event.setStatus(order.getStatus().toString());

            // Fetch user email from user service
            try {
                ApiResponse<String> emailResponse = userClient.getUserEmailById(order.getUserId());
                if (emailResponse != null && emailResponse.isSuccess()) {
                    event.setUserEmail(emailResponse.getData());
                    log.info("Fetched user email for OrderPlacedEvent: {}", emailResponse.getData());
                } else {
                    log.warn("Failed to fetch user email for OrderPlacedEvent, setting to null");
                    event.setUserEmail(null);
                }
            } catch (Exception e) {
                log.warn("Error fetching user email for OrderPlacedEvent: {}", e.getMessage());
                event.setUserEmail(null);
            }

            // Fetch and populate order items
            try {
                List<OrderItemDto> orderItems = orderItemService.fetchCartItemsForOrder(order.getCartId(), order.getUserId());
                List<OrderPlacedEvent.OrderItemInfo> orderItemInfos = convertToOrderItemInfos(orderItems);
                event.setOrderItems(orderItemInfos);
                log.info("Added {} order items to OrderPlacedEvent", orderItemInfos.size());
            } catch (Exception e) {
                log.warn("Failed to fetch order items for OrderPlacedEvent: {}", e.getMessage());
                event.setOrderItems(List.of()); // Set empty list if items can't be fetched
            }

            // Publish event to RabbitMQ
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_PLACED_ROUTING_KEY,
                    event
            );

            log.info("OrderPlacedEvent published successfully for order: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to publish OrderPlacedEvent for order {}: {}", 
                    order.getOrderNumber(), e.getMessage(), e);
            // Don't throw exception to avoid breaking the order submission flow
        }
    }

    /**
     * Convert OrderItemDto list to OrderItemInfo list for the event
     */
    private List<OrderPlacedEvent.OrderItemInfo> convertToOrderItemInfos(List<OrderItemDto> orderItems) {
        return orderItems.stream()
                .map(this::convertToOrderItemInfo)
                .collect(Collectors.toList());
    }

    /**
     * Convert single OrderItemDto to OrderItemInfo
     */
    private OrderPlacedEvent.OrderItemInfo convertToOrderItemInfo(OrderItemDto orderItem) {
        OrderPlacedEvent.OrderItemInfo itemInfo = new OrderPlacedEvent.OrderItemInfo();
        itemInfo.setProductId(orderItem.getProductId());

        // Fetch actual product name from product service
        String productName = "Unknown Product";
        try {
            ApiResponse<String> nameResponse = productClient.getProductNameById(orderItem.getProductId());
            if (nameResponse != null && nameResponse.isSuccess() && nameResponse.getData() != null) {
                productName = nameResponse.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch product name for ID {}: {}", orderItem.getProductId(), e.getMessage());
        }
        itemInfo.setProductName(productName);

        itemInfo.setQuantity(orderItem.getQuantity());
        itemInfo.setPrice(orderItem.getPrice());
        itemInfo.setSubtotal(orderItem.getSubtotal());
        itemInfo.setProductImage(""); // This would need to be fetched from product service
        return itemInfo;
    }
}
