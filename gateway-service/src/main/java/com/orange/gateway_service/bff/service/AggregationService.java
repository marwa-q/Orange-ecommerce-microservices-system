package com.orange.gateway_service.bff.service;

import com.orange.gateway_service.bff.dto.product.ProductAggregateDto;
import com.orange.gateway_service.bff.dto.product.ProductPageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationService {

    private final BffClient client;

    public Mono<ProductPageDto> aggregateProducts(Map<String, String> queryParams) {
        return client.listProducts(queryParams)
            .flatMap(productsResponse -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) productsResponse.get("data");
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> products = (List<Map<String, Object>>) dataMap.get("products");
                List<ProductAggregateDto> aggregatedProducts = products.stream().map(p -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> categoryMap = (Map<String, Object>) p.get("category");
                    
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> tagsList = (List<Map<String, Object>>) p.get("tags");
                    
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> reviewsList = (List<Map<String, Object>>) p.get("reviews");
                    
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> variantsList = (List<Map<String, Object>>) p.get("variants");
                    
                    return ProductAggregateDto.builder()
                        .id(p.get("uuid") != null ? p.get("uuid").toString() : null)
                        .name(p.get("name") != null ? p.get("name").toString() : null)
                        .category(categoryMap != null ? new ProductAggregateDto.CategorySummaryDto(
                            parseUUID(categoryMap.get("uuid")),
                            categoryMap.get("name") != null ? categoryMap.get("name").toString() : null
                        ) : null)
                        .price(parseBigDecimal(p.get("price")))
                        .imageUrl(p.get("image") != null ? p.get("image").toString() : null)
                        .stock(p.get("stock") != null ? ((Number) p.get("stock")).intValue() : null)
                        .isDeleted(p.get("isDeleted") != null ? (Boolean) p.get("isDeleted") : null)
                        .viewCount(p.get("viewCount") != null ? ((Number) p.get("viewCount")).longValue() : null)
                        .rate(parseBigDecimal(p.get("rate")))
                        .createdAt(parseLocalDateTime(p.get("createdAt")))
                        .updatedAt(parseLocalDateTime(p.get("updatedAt")))
                        .tags(tagsList != null ? tagsList.stream()
                            .map(tag -> new ProductAggregateDto.TagSummaryDto(
                                parseUUID(tag.get("uuid")),
                                tag.get("name") != null ? tag.get("name").toString() : null
                            )).collect(Collectors.toList()) : new ArrayList<>())
                        .reviews(reviewsList != null ? reviewsList.stream()
                            .map(review -> new ProductAggregateDto.ReviewSummaryDto(
                                parseUUID(review.get("uuid")),
                                parseUUID(review.get("userId")),
                                parseBigDecimal(review.get("rate"))
                            )).collect(Collectors.toList()) : new ArrayList<>())
                        .variants(variantsList != null ? variantsList.stream()
                            .map(variant -> new com.orange.gateway_service.bff.dto.product.ProductVariant(
                                variant.get("variantId") != null ? variant.get("variantId").toString() : null,
                                variant.get("name") != null ? variant.get("name").toString() : null,
                                variant.get("attributes") != null ? (Map<String, String>) variant.get("attributes") : null,
                                parseBigDecimal(variant.get("price")),
                                variant.get("stock") != null ? ((Number) variant.get("stock")).intValue() : null,
                                variant.get("sku") != null ? variant.get("sku").toString() : null,
                                variant.get("image") != null ? variant.get("image").toString() : null,
                                variant.get("isActive") != null ? (Boolean) variant.get("isActive") : null
                            )).collect(Collectors.toList()) : new ArrayList<>())
                        .build();
                }).collect(Collectors.toList());
                
                return Mono.just(ProductPageDto.builder()
                    .products(aggregatedProducts)
                    .currentPage(((Number) dataMap.get("currentPage")).intValue())
                    .totalPages(((Number) dataMap.get("totalPages")).intValue())
                    .totalElements(((Number) dataMap.get("totalElements")).longValue())
                    .pageSize(((Number) dataMap.get("pageSize")).intValue())
                    .hasNext((Boolean) dataMap.get("hasNext"))
                    .hasPrevious((Boolean) dataMap.get("hasPrevious"))
                    .isFirst((Boolean) dataMap.get("isFirst"))
                    .isLast((Boolean) dataMap.get("isLast"))
                    .build());
            });
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal b) return b;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }

    private LocalDateTime parseLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof String) {
            try {
                return java.time.LocalDateTime.parse((String) value);
            } catch (Exception e) {
                log.warn("Failed to parse LocalDateTime from string: {}", value, e);
                return null;
            }
        }
        return null;
    }

    private UUID parseUUID(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) {
            return (UUID) value;
        }
        if (value instanceof String) {
            try {
                return java.util.UUID.fromString((String) value);
            } catch (Exception e) {
                log.warn("Failed to parse UUID from string: {}", value, e);
                return null;
            }
        }
        return null;
    }
}

