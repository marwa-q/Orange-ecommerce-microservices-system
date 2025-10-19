package com.orange.order_service.order.controller;

import com.orange.order_service.common.dto.ApiResponse;
import com.orange.order_service.order.dto.SubmitOrderRequest;
import com.orange.order_service.order.dto.CreateOrderRequest;
import com.orange.order_service.order.dto.OrderResponse;
import com.orange.order_service.order.dto.OrderWithItemsResponse;
import com.orange.order_service.order.dto.PaginatedOrderResponse;
import com.orange.order_service.order.service.OrderService;
import com.orange.order_service.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get User Orders", description = "Get paginated orders for the authenticated user with items")
    public ResponseEntity<ApiResponse<PaginatedOrderResponse>> getUserOrders(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            UUID userId = getCurrentUserId(httpRequest);
            log.info("Received get user orders request for user: {} (page: {}, size: {})", userId, page, size);

            ApiResponse<PaginatedOrderResponse> response = orderService.getUserOrders(userId, httpRequest, page, size);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error fetching user orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("order.unauthorized"));
        }
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel Order", description = "Cancel an order if it hasn't been shipped yet")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable("id") UUID orderId,
            HttpServletRequest httpRequest) {

        try {
            UUID userId = getCurrentUserId(httpRequest);
            log.info("Received cancel order request for order {} by user {}", orderId, userId);

            ApiResponse<OrderResponse> response = orderService.cancelOrder(orderId, userId);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error cancelling order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("order.unauthorized"));
        }
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit Order", description = "Submit an order by user - update payment method and address")
    public ResponseEntity<ApiResponse<OrderResponse>> submitOrder(
            @PathVariable("id") UUID orderId,
            @Valid @RequestBody SubmitOrderRequest request,
            HttpServletRequest httpRequest) {

        try {
            UUID userId = getCurrentUserId(httpRequest);
            log.info("Received submit order request for order {} by user {}", orderId, userId);

            ApiResponse<OrderResponse> response = orderService.submitOrder(orderId, userId, request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error submitting order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("order.submit_error"));
        }
    }

    // Function to extract user uuid from token
    public UUID getCurrentUserId(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            log.info("=== DEBUG: Authorization header: '{}' ===", authHeader);
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.info("=== DEBUG: Extracted token: '{}' ===", token);
                
                UUID userId = jwtUtil.getUserId(token);
                log.info("=== DEBUG: Extracted User ID: '{}', Type: {} ===", 
                        userId, userId != null ? userId.getClass().getSimpleName() : "null");
                
                if (userId != null) {
                    return userId;
                }
            }
            throw new RuntimeException("Unable to extract user ID from token");
        } catch (Exception e) {
            log.error("=== DEBUG: Error extracting user ID: {} ===", e.getMessage());
            throw new RuntimeException("User not authenticated: " + e.getMessage());
        }
    }
}