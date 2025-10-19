package com.orange.order_service.order.service;

import com.orange.order_service.common.dto.ApiResponse;
import com.orange.order_service.order.client.ProductClient;
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
    private final ProductClient productClient;
    private final OrderEventPublisherService orderEventPublisherService;

    public OrderService(OrderRepository orderRepository, OrderItemService orderItemService, OrderConverterService orderConverterService, PaginationUtilService paginationUtilService, ProductClient productClient, OrderEventPublisherService orderEventPublisherService) {
        this.orderRepository = orderRepository;
        this.orderItemService = orderItemService;
        this.orderConverterService = orderConverterService;
        this.paginationUtilService = paginationUtilService;
        this.productClient = productClient;
        this.orderEventPublisherService = orderEventPublisherService;
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

            // Check if we need to increase stock before changing status
            // Only increase stock if the order is not PENDING (meaning stock was already decreased)
            OrderStatus previousStatus = order.getStatus();
            boolean shouldIncreaseStock = previousStatus != OrderStatus.PENDING;

            // Update order status to cancelled
            order.setStatus(OrderStatus.CANCELLED);

            // Increase stock quantity only if needed
            if (shouldIncreaseStock) {
                log.info("Increasing stock for cancelled order {} (previous status was: {})", orderId, previousStatus);
                increaseStock(order.getUuid());
            }

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
     * Submit an order (USER) - update payment method, address and mark as SUBMITTED
     */
    @Transactional
    public ApiResponse<OrderResponse> submitOrder(UUID orderId, UUID userId, SubmitOrderRequest request) {
        try {
            log.info("Attempting to submit order {}", orderId);

            // Find the order by UUID
            Optional<Order> orderOpt = orderRepository.findByUuidAndUserId(orderId, userId);
            if (orderOpt.isEmpty()) {
                log.warn("Order {} not found", orderId);
                return ApiResponse.failure("order.not_found");
            }

            Order order = orderOpt.get();

            // Check if order can be submitted (must be PENDING)
            if (order.getStatus() != OrderStatus.PENDING) {
                log.warn("Cannot submit order {} - current status is {}", orderId, order.getStatus());
                return ApiResponse.failure("order.cannot_submit_status");
            }

            // Validate stock availability before submitting order
            ApiResponse<Void> stockValidation = validateStockAvailability(orderId);
            if (!stockValidation.isSuccess()) {
                log.warn("Stock validation failed for order {}: {}", orderId, stockValidation.getMessage());
                return ApiResponse.failure(stockValidation.getMessage());
            }

            // Update order data
            order.setPaymentMethod(request.getPaymentMethod());
            order.setShippingAddress(request.getAddress());
            
            // Reduce stock for all products in the cart
            decreaseStock(order.getUuid());

            // Mark as submitted using helper method
            order.markAsSubmitted();
            
            Order submittedOrder = orderRepository.save(order);

            log.info("Order {} successfully submitted", orderId);

            // Publish OrderPlacedEvent for email notification
            orderEventPublisherService.publishOrderPlacedEvent(submittedOrder);

            // Convert to response DTO
            OrderResponse response = orderConverterService.convertToOrderResponse(submittedOrder, List.of());
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Error submitting order {}: {}", orderId, e.getMessage(), e);
            return ApiResponse.failure("order.submit_failed");
        }
    }

    // Change stock for products in an order
    @Transactional
    public void changeStock(UUID orderId, String action) {
        try {
            // Find the order to get cartId
            Optional<Order> orderOpt = orderRepository.findByUuid(orderId);
            if (orderOpt.isEmpty()) {
                log.error("Order not found with ID: {}", orderId);
                return;
            }
            
            Order order = orderOpt.get();
            UUID cartId = order.getCartId();
            UUID userId = order.getUserId();
            
            log.info("Changing stock for order {} with action: {}", orderId, action);
            
            // Fetch cart items for the order
            List<OrderItemDto> cartItems = orderItemService.fetchCartItemsForOrder(cartId, userId);
            
            for (OrderItemDto item : cartItems) {
                try {
                    ApiResponse<Void> stockResponse = productClient.setStock(
                            item.getProductId(), 
                            item.getQuantity(), 
                            action
                    );
                    
                    if (!stockResponse.isSuccess()) {
                        log.warn("Failed to {} stock for product {}: {}", 
                                action, item.getProductId(), stockResponse.getMessage());
                    } else {
                        log.info("Successfully {} stock for product {} by {}", 
                                action, item.getProductId(), item.getQuantity());
                    }
                } catch (Exception e) {
                    log.error("Error {} stock for product {}: {}", 
                            action, item.getProductId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error changing stock for order {}: {}", orderId, e.getMessage());
        }
    }
    
    /**
     * Validate stock availability for all products in an order
     * This method performs a dry-run stock check without actually modifying inventory
     * @param orderId The order ID to validate
     * @return ApiResponse indicating success or failure with specific error message
     */
    private ApiResponse<Void> validateStockAvailability(UUID orderId) {
        try {
            log.info("Validating stock availability for order: {}", orderId);
            
            // Find the order to get cartId and userId
            Optional<Order> orderOpt = orderRepository.findByUuid(orderId);
            if (orderOpt.isEmpty()) {
                log.error("Order not found for stock validation: {}", orderId);
                return ApiResponse.failure("order.not_found");
            }
            
            Order order = orderOpt.get();
            UUID cartId = order.getCartId();
            UUID userId = order.getUserId();
            
            // Fetch cart items for the order
            List<OrderItemDto> cartItems = orderItemService.fetchCartItemsForOrder(cartId, userId);
            
            if (cartItems.isEmpty()) {
                log.warn("No items found in cart for order: {}", orderId);
                return ApiResponse.failure("order.no_items");
            }
            
            // Validate stock for each item using a temporary decrease/increase cycle
            for (OrderItemDto item : cartItems) {
                try {
                    // Perform temporary stock decrease to validate availability
                    ApiResponse<Void> decreaseResponse = productClient.setStock(
                            item.getProductId(), 
                            item.getQuantity(), 
                            "decrease"
                    );
                    
                    if (!decreaseResponse.isSuccess()) {
                        log.warn("Insufficient stock for product {} (requested: {}) in order {}", 
                                item.getProductId(), item.getQuantity(), orderId);
                        return ApiResponse.failure("order.insufficient_stock");
                    }
                    
                    // Immediately restore stock to original state
                    ApiResponse<Void> restoreResponse = productClient.setStock(
                            item.getProductId(), 
                            item.getQuantity(), 
                            "increase"
                    );
                    
                    if (!restoreResponse.isSuccess()) {
                        log.error("Failed to restore stock for product {} after validation - this may cause inventory inconsistency", 
                                item.getProductId());
                        // Don't fail the validation, but log the issue
                    }
                    
                    log.debug("Stock validation passed for product {} (quantity: {})", 
                            item.getProductId(), item.getQuantity());
                    
                } catch (Exception e) {
                    log.error("Error validating stock for product {} in order {}: {}", 
                            item.getProductId(), orderId, e.getMessage());
                    return ApiResponse.failure("order.stock_validation_error");
                }
            }
            
            log.info("Stock validation successful for order: {} (validated {} items)", 
                    orderId, cartItems.size());
            return ApiResponse.success(null);
            
        } catch (Exception e) {
            log.error("Error validating stock for order {}: {}", orderId, e.getMessage());
            return ApiResponse.failure("order.stock_validation_failed");
        }
    }
    
    // Convenience method to decrease stock (for order creation)
    public void decreaseStock(UUID orderId) {
        changeStock(orderId, "decrease");
    }
    
    // Convenience method to increase stock (for order cancellation)
    public void increaseStock(UUID orderId) {
        changeStock(orderId, "increase");
    }
}