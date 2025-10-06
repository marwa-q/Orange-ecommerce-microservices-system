package com.orange.product_service.product.dto;

import java.util.UUID;

public record DeleteProductRequest (
        UUID productId
){
}
