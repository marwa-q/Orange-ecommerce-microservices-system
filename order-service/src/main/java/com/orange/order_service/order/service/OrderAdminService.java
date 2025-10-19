package com.orange.order_service.order.service;

import com.orange.order_service.common.dto.ApiResponse;
import com.orange.order_service.order.dto.OrderWithItemsResponse;
import com.orange.order_service.order.dto.PaginatedOrderResponse;
import com.orange.order_service.order.entity.Order;
import com.orange.order_service.order.entity.OrderStatus;
import com.orange.order_service.order.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderAdminService {

    private final OrderRepository orderRepository;
    private final OrderConverterService orderConverterService;
    private final OrderItemService orderItemService;
    private final PaginationUtilService paginationUtilService;

    public OrderAdminService(OrderRepository orderRepository, OrderConverterService orderConverterService, OrderItemService orderItemService, PaginationUtilService paginationUtilService) {
        this.orderRepository = orderRepository;
        this.orderConverterService = orderConverterService;
        this.orderItemService = orderItemService;
        this.paginationUtilService = paginationUtilService;
    }

    /**
     * Get all submitted orders with items (ADMIN only) - Paginated
     */
    public ApiResponse<PaginatedOrderResponse> getSubmittedOrders(HttpServletRequest request, int page, int size) {
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0) size = 10;
            if (size > 100) size = 100; // Limit max page size

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size);

            log.info("Fetching submitted orders with items for admin (page: {}, size: {})", page, size);

            // Get paginated submitted orders
            Page<Order> submittedOrdersPage = orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.SUBMITTED, pageable);

            if (submittedOrdersPage.isEmpty()) {
                log.info("No submitted orders found");
                return ApiResponse.success(paginationUtilService.createEmptyPaginatedResponse(page, size));
            }

            // Convert orders to response DTOs with items
            List<OrderWithItemsResponse> orderResponses = submittedOrdersPage.getContent().stream()
                    .map(order -> {
                        try {
                            // Fetch cart items for each order
                            return orderItemService.convertToOrderWithItemsResponse(order, order.getUserId());
                        } catch (Exception e) {
                            log.warn("Could not fetch items for order {}: {}", order.getOrderNumber(), e.getMessage());
                            // Return order without items if cart service is unavailable
                            return orderConverterService.convertToOrderWithItemsResponse(order, List.of());
                        }
                    })
                    .collect(Collectors.toList());

            // Create paginated response
            PaginatedOrderResponse paginatedResponse = paginationUtilService.createPaginatedResponse(submittedOrdersPage, orderResponses);

            log.info("Found {} submitted orders (page {}/{})", orderResponses.size(), page + 1, submittedOrdersPage.getTotalPages());
            return ApiResponse.success(paginatedResponse);

        } catch (Exception e) {
            log.error("Error fetching submitted orders: {}", e.getMessage(), e);
            return ApiResponse.failure("order.fetch_submitted_failed");
        }
    }

    /**
     * Get all orders from all users (ADMIN only) - Paginated
     */
    public ApiResponse<PaginatedOrderResponse> getAllOrders(HttpServletRequest request, int page, int size) {
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0) size = 10;
            if (size > 100) size = 100; // Limit max page size

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size);

            log.info("Fetching all orders for admin (page: {}, size: {})", page, size);

            // Get paginated all orders
            Page<Order> allOrdersPage = orderRepository.findAllOrderByCreatedAtDesc(pageable);

            if (allOrdersPage.isEmpty()) {
                log.info("No orders found");
                return ApiResponse.success(paginationUtilService.createEmptyPaginatedResponse(page, size));
            }

            // Convert orders to response DTOs with items
            List<OrderWithItemsResponse> orderResponses = allOrdersPage.getContent().stream()
                    .map(order -> {
                        try {
                            // Fetch cart items for each order
                            return orderItemService.convertToOrderWithItemsResponse(order, order.getUserId());
                        } catch (Exception e) {
                            log.warn("Could not fetch items for order {}: {}", order.getOrderNumber(), e.getMessage());
                            // Return order without items if cart service is unavailable
                            return orderConverterService.convertToOrderWithItemsResponse(order, List.of());
                        }
                    })
                    .collect(Collectors.toList());

            // Create paginated response
            PaginatedOrderResponse paginatedResponse = paginationUtilService.createPaginatedResponse(allOrdersPage, orderResponses);

            log.info("Found {} orders (page {}/{})", orderResponses.size(), page + 1, allOrdersPage.getTotalPages());
            return ApiResponse.success(paginatedResponse);

        } catch (Exception e) {
            log.error("Error fetching all orders: {}", e.getMessage(), e);
            return ApiResponse.failure("order.fetch_all_failed");
        }
    }
}
