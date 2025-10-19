package com.orange.order_service.order.service;

import com.orange.order_service.order.dto.OrderItemDto;
import com.orange.order_service.order.dto.OrderResponse;
import com.orange.order_service.order.dto.OrderWithItemsResponse;
import com.orange.order_service.order.entity.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderConverterService {

    /**
     * Convert Order entity to OrderResponse DTO
     */
    public OrderResponse convertToOrderResponse(Order order) {
        return convertToOrderResponse(order, List.of());
    }

    /**
     * Convert Order entity to OrderResponse DTO with items
     */
    public OrderResponse convertToOrderResponse(Order order, List<OrderItemDto> items) {
        OrderResponse response = new OrderResponse();
        response.setUuid(order.getUuid());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUserId());
        response.setCartId(order.getCartId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "UNKNOWN");
        response.setShippingAddress(order.getShippingAddress());
        response.setGiftMessage(order.getGiftMessage());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setItems(items);
        return response;
    }

    /**
     * Convert Order entity to OrderWithItemsResponse DTO
     */
    public OrderWithItemsResponse convertToOrderWithItemsResponse(Order order, List<OrderItemDto> items) {
        OrderWithItemsResponse response = new OrderWithItemsResponse();
        response.setUuid(order.getUuid());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUserId());
        response.setCartId(order.getCartId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "UNKNOWN");
        response.setShippingAddress(order.getShippingAddress());
        response.setGiftMessage(order.getGiftMessage());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setItems(items);
        return response;
    }
}
