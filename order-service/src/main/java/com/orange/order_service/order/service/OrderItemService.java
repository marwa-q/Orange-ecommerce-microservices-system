package com.orange.order_service.order.service;

import com.orange.order_service.common.dto.ApiResponse;
import com.orange.order_service.order.client.CartClient;
import com.orange.order_service.order.dto.CartItemResponseDto;
import com.orange.order_service.order.dto.OrderItemDto;
import com.orange.order_service.order.dto.OrderWithItemsResponse;
import com.orange.order_service.order.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderItemService {

    private final CartClient cartClient;

    public OrderItemService(CartClient cartClient) {
        this.cartClient = cartClient;
    }

    // Convert order to order with items response
    public OrderWithItemsResponse convertToOrderWithItemsResponse(Order order, UUID userId) {
        OrderWithItemsResponse response = new OrderWithItemsResponse();
        response.setUuid(order.getUuid());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUserId());
        response.setCartId(order.getCartId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        if(order.getPaymentMethod() != null) 
            response.setPaymentMethod(order.getPaymentMethod().name());
        response.setShippingAddress(order.getShippingAddress());
        response.setGiftMessage(order.getGiftMessage());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // Try to fetch cart items from cart service
        try {
            List<OrderItemDto> items = fetchCartItemsForOrder(order.getCartId(), userId);
            response.setItems(items);
        } catch (Exception e) {
            log.warn("Could not fetch cart items for order {}: {}", order.getOrderNumber(), e.getMessage());
            // Set empty items list if cart service is unavailable
            response.setItems(List.of());
        }

        return response;
    }

    // Fetch order items from cart service
    public List<OrderItemDto> fetchCartItemsForOrder(UUID cartId, UUID userId) {
        try {
            // Fetch cart items from cart service
            ApiResponse<List<CartItemResponseDto>> cartResponse = cartClient.getCartItemsByCartId(
                    cartId.toString());

            if (cartResponse.isSuccess() && cartResponse.getData() != null) {
                // Convert cart items to order items
                return cartResponse.getData().stream()
                        .map(this::convertCartItemToOrderItem)
                        .collect(Collectors.toList());
            }

            return List.of();
        } catch (Exception e) {
            log.error("Error fetching cart items for cart {}: {}", cartId, e.getMessage());
            return List.of();
        }
    }

    // Convert cart items to order items DTO
    public OrderItemDto convertCartItemToOrderItem(CartItemResponseDto cartItem) {
        OrderItemDto orderItem = new OrderItemDto();
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setProductName(cartItem.getProductName());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getPrice());
        orderItem.setSubtotal(cartItem.getSubtotal());
        return orderItem;
    }
}
