package com.orange.order_service.order.service;

import com.orange.order_service.common.dto.ApiResponse;
import com.orange.order_service.order.dto.*;
import com.orange.order_service.order.entity.Order;
import com.orange.order_service.order.entity.OrderStatus;
import com.orange.order_service.order.entity.PaymentMethod;
import com.orange.order_service.order.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final OrderConverterService orderConverterService;
    private final PaginationUtilService paginationUtilService;

    public OrderService(OrderRepository orderRepository, OrderItemService orderItemService, OrderConverterService orderConverterService, PaginationUtilService paginationUtilService) {
        this.orderRepository = orderRepository;
        this.orderItemService = orderItemService;
        this.orderConverterService = orderConverterService;
        this.paginationUtilService = paginationUtilService;
    }

    /**
     * Create order from cart checkout event
     */
    @Transactional
    public ApiResponse<OrderResponse> createOrderFromCartCheckout(UUID cartId, UUID userId, BigDecimal totalAmount) {
        try {
            log.info("Creating order from cart checkout event for cart: {} and user: {}", cartId, userId);

            // Check if order already exists for this cart
            Optional<Order> existingOrder = orderRepository.findByCartUuidQuery(cartId);
            if (existingOrder.isPresent()) {
                log.warn("Order already exists for cart: {}", cartId);
                return ApiResponse.failure("order.already_exists");
            }

            // Create new order
            Order order = new Order();
            order.setUserId(userId);
            order.setCartId(cartId);
            order.setStatus(OrderStatus.PENDING);
            order.setTotalAmount(totalAmount);
            // Set default values for new fields (can be updated later)
            order.setPaymentMethod(PaymentMethod.CACHE_ON_DELIVERY);
            order.setShippingAddress("To be provided");

            // Save order
            Order savedOrder = orderRepository.save(order);
            

            // Convert to response DTO (items will be empty since we don't store them)
            OrderResponse response = orderConverterService.convertToOrderResponse(savedOrder, List.of());

            log.info("Order created from cart checkout with number: {}", savedOrder.getOrderNumber());
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Error creating order from cart checkout: {}", e.getMessage(), e);
            return ApiResponse.failure("order.creation_failed");
        }
    }

    /**
     * Get all orders for a user with their items (paginated)
     */
    public ApiResponse<PaginatedOrderResponse> getUserOrders(UUID userId, HttpServletRequest request, int page, int size) {
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0) size = 10;
            if (size > 100) size = 100; // Limit max page size

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size);

            // Get paginated orders for the user
            Page<Order> orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

            if (orderPage.isEmpty()) {
                log.warn("No orders found for user: {}", userId);
                return ApiResponse.success(paginationUtilService.createEmptyPaginatedResponse(page, size));
            }

            // Convert orders to response DTOs with items
            List<OrderWithItemsResponse> orderResponses = orderPage.getContent().stream()
                    .map(order -> orderItemService.convertToOrderWithItemsResponse(order, userId))
                    .collect(Collectors.toList());

            // Create paginated response
            PaginatedOrderResponse paginatedResponse = paginationUtilService.createPaginatedResponse(orderPage, orderResponses);

            log.info("Found {} orders for user: {} (page {}/{})", orderResponses.size(), userId, page + 1, orderPage.getTotalPages());
            return ApiResponse.success(paginatedResponse);

        } catch (Exception e) {
            log.error("Error fetching orders for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.failure("order.fetch_failed");
        }
    }

    /**
     * Cancel an order if it hasn't been shipped yet
     */
    @Transactional
    public ApiResponse<OrderResponse> cancelOrder(UUID orderId, UUID userId) {
        try {
            log.info("Attempting to cancel order {} for user {}", orderId, userId);

            // Find the order by ID and user ID to ensure user can only cancel their own orders
            Optional<Order> orderOpt = orderRepository.findByUuidAndUserId(orderId, userId);
            if (orderOpt.isEmpty()) {
                log.warn("Order {} not found for user {}", orderId, userId);
                return ApiResponse.failure("order.not_found");
            }

            Order order = orderOpt.get();

            // Check if order can be cancelled (not shipped yet)
            if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
                log.warn("Cannot cancel order {} - already shipped or delivered", orderId);
                return ApiResponse.failure("order.cannot_cancel_shipped");
            }

            // Check if order is already cancelled
            if (order.getStatus() == OrderStatus.CANCELLED) {
                log.warn("Order {} is already cancelled", orderId);
                return ApiResponse.failure("order.already_cancelled");
            }

            // Update order status to cancelled
            order.setStatus(OrderStatus.CANCELLED);
            Order cancelledOrder = orderRepository.save(order);

            log.info("Order {} successfully cancelled for user {}", orderId, userId);

            // Convert to response DTO
            OrderResponse response = orderConverterService.convertToOrderResponse(cancelledOrder, List.of());
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Error cancelling order {}: {}", orderId, e.getMessage(), e);
            return ApiResponse.failure("order.cancel_failed");
        }
    }

    /**
     * Confirm an order (USER) - update payment method, address and mark as CONFIRMED
     */
    @Transactional
    public ApiResponse<OrderResponse> confirmOrder(UUID orderId, UUID userId, ConfirmOrderRequest request) {
        try {
            log.info("Attempting to confirm order {}", orderId);

            // Find the order by UUID
            Optional<Order> orderOpt = orderRepository.findByUuidAndUserId(orderId, userId);
            if (orderOpt.isEmpty()) {
                log.warn("Order {} not found", orderId);
                return ApiResponse.failure("order.not_found");
            }

            Order order = orderOpt.get();

            // Check if order can be confirmed (must be PENDING)
            if (order.getStatus() != OrderStatus.PENDING) {
                log.warn("Cannot confirm order {} - current status is {}", orderId, order.getStatus());
                return ApiResponse.failure("order.cannot_confirm_status");
            }

            // Update order data
            order.setPaymentMethod(request.getPaymentMethod());
            order.setShippingAddress(request.getAddress());
            
            // Mark as confirmed using helper method
            order.markAsConfirmed();
            
            Order confirmedOrder = orderRepository.save(order);

            log.info("Order {} successfully confirmed", orderId);

            // Convert to response DTO
            OrderResponse response = orderConverterService.convertToOrderResponse(confirmedOrder, List.of());
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Error confirming order {}: {}", orderId, e.getMessage(), e);
            return ApiResponse.failure("order.confirm_failed");
        }
    }
}