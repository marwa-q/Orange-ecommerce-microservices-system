package com.orange.order_service.order.service;

import com.orange.order_service.order.dto.OrderWithItemsResponse;
import com.orange.order_service.order.dto.PaginatedOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaginationUtilService {

    /**
     * Create paginated response from Page and OrderWithItemsResponse list
     */
    public PaginatedOrderResponse createPaginatedResponse(Page<?> page, List<OrderWithItemsResponse> orderResponses) {
        PaginatedOrderResponse paginatedResponse = new PaginatedOrderResponse();
        paginatedResponse.setOrders(orderResponses);
        paginatedResponse.setCurrentPage(page.getNumber());
        paginatedResponse.setTotalPages(page.getTotalPages());
        paginatedResponse.setTotalElements(page.getTotalElements());
        paginatedResponse.setSize(page.getSize());
        paginatedResponse.setFirst(page.isFirst());
        paginatedResponse.setLast(page.isLast());
        paginatedResponse.setHasNext(page.hasNext());
        paginatedResponse.setHasPrevious(page.hasPrevious());
        return paginatedResponse;
    }

    /**
     * Create empty paginated response
     */
    public PaginatedOrderResponse createEmptyPaginatedResponse(int page, int size) {
        PaginatedOrderResponse response = new PaginatedOrderResponse();
        response.setOrders(List.of());
        response.setCurrentPage(page);
        response.setTotalPages(0);
        response.setTotalElements(0);
        response.setSize(size);
        response.setFirst(true);
        response.setLast(true);
        response.setHasNext(false);
        response.setHasPrevious(false);
        return response;
    }
}
