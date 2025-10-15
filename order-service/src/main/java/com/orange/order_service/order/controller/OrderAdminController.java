package com.orange.order_service.order.controller;

import com.orange.order_service.common.dto.ApiResponse;
import com.orange.order_service.order.dto.OrderWithItemsResponse;
import com.orange.order_service.order.dto.PaginatedOrderResponse;
import com.orange.order_service.order.service.OrderAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/orders/admin")
@Slf4j
@Tag(name = "Admin Order Management", description = "Admin APIs for managing orders")
public class OrderAdminController {

    private final OrderAdminService orderAdminService;

    public OrderAdminController(OrderAdminService orderAdminService) {
        this.orderAdminService = orderAdminService;
    }

    @GetMapping("/confirmed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Confirmed Orders", description = "Get paginated confirmed orders with items (ADMIN only)")
    public ResponseEntity<ApiResponse<PaginatedOrderResponse>> getConfirmedOrders(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Received get confirmed orders request by admin (page: {}, size: {})", page, size);

            ApiResponse<PaginatedOrderResponse> response = orderAdminService.getConfirmedOrders(request, page, size);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error fetching confirmed orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("order.fetch_confirmed_error"));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All Orders", description = "Get paginated all orders from all users with items (ADMIN only)")
    public ResponseEntity<ApiResponse<PaginatedOrderResponse>> getAllOrders(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Received get all orders request by admin (page: {}, size: {})", page, size);

            ApiResponse<PaginatedOrderResponse> response = orderAdminService.getAllOrders(request, page, size);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error fetching all orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("order.fetch_all_error"));
        }
    }
}