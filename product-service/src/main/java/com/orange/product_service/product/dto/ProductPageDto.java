package com.orange.product_service.product.dto;

import java.util.List;

public record ProductPageDto(
        List<ProductDto> products,
        int currentPage,
        int totalPages,
        long totalElements,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious,
        boolean isFirst,
        boolean isLast
) {
}
