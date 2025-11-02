package com.orange.gateway_service.bff.service;

import com.orange.gateway_service.bff.dto.product.ProductAggregateDto;
import com.orange.gateway_service.bff.dto.product.ProductPageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationService {

    private final BffClient client;

    public Mono<ProductPageDto> aggregateProducts(Map<String, String> queryParams) {
        Mono<Map> productsMono = client.listProducts(queryParams);
        return productsMono
            .doOnNext(response -> log.info("Product service response: {}", response))
            .flatMap(productsResponse -> {
                log.info("Processing product response. Response keys: {}", productsResponse.keySet());
                
                // Safely extract the data field, handling both List and nested Map structures
                Object dataObj = productsResponse.get("data");
                log.info("Data object type: {}, value: {}", 
                    dataObj != null ? dataObj.getClass().getSimpleName() : "null", dataObj);
                
                List<Map<String, Object>> items = new ArrayList<>();
                Map<String, Object> pageData;
                
                if (dataObj == null) {
                    log.warn("Data field is null in product service response");
                    return Mono.just(ProductPageDto.builder()
                        .products(List.of())
                        .currentPage(0)
                        .totalPages(0)
                        .totalElements(0)
                        .pageSize(10)
                        .hasNext(false)
                        .hasPrevious(false)
                        .isFirst(true)
                        .isLast(true)
                        .build());
                }
                
                if (dataObj instanceof Map) {
                    // Handle ProductPageDto structure from product service
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                    log.info("Data is a map with keys: {}", dataMap.keySet());
                    
                    // Check if this is ProductPageDto with 'products' field
                    Object productsField = dataMap.get("products");
                    if (productsField instanceof List) {
                        // This is ProductPageDto structure
                        pageData = dataMap;
                        @SuppressWarnings("unchecked")
                        List<Object> productsList = (List<Object>) productsField;
                        log.info("Found ProductPageDto with {} products", productsList.size());
                        for (Object item : productsList) {
                            if (item instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> itemMap = (Map<String, Object>) item;
                                items.add(itemMap);
                            }
                        }
                    } else {
                        pageData = null;
                        // Check for common pagination field names (fallback)
                        Object content = dataMap.getOrDefault("content", 
                            dataMap.getOrDefault("items", 
                            dataMap.getOrDefault("data", null)));
                        
                        if (content instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Object> contentList = (List<Object>) content;
                            log.info("Found content list with {} items", contentList.size());
                            for (Object item : contentList) {
                                if (item instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> itemMap = (Map<String, Object>) item;
                                    items.add(itemMap);
                                }
                            }
                        } else {
                            log.warn("Content field is not a list. Type: {}", 
                                content != null ? content.getClass().getSimpleName() : "null");
                        }
                    }
                } else {
                    pageData = null;
                    if (dataObj instanceof List) {
                        // Direct list response (fallback)
                        @SuppressWarnings("unchecked")
                        List<Object> rawList = (List<Object>) dataObj;
                        log.info("Data is a direct list with {} items", rawList.size());
                        for (Object item : rawList) {
                            if (item instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> itemMap = (Map<String, Object>) item;
                                items.add(itemMap);
                            }
                        }
                    } else {
                        log.warn("Data field is neither List nor Map. Type: {}",
                            dataObj.getClass().getSimpleName());
                    }
                }
                
                log.info("Extracted {} items from product response", items.size());
            
            List<String> productIds = items.stream()
                .map(m -> {
                    String uuid = Objects.toString(m.get("uuid"), null);
                    return uuid != null ? uuid : Objects.toString(m.get("id"), null);
                })
                .filter(Objects::nonNull)
                .toList();

            String csv = productIds.isEmpty() ? "" : String.join(",", productIds);
            Mono<Map> reviewsMono = productIds.isEmpty() 
                ? Mono.just(Map.of("data", Map.of()))
                : client.reviewsSummary(csv).onErrorReturn(Map.of("data", Map.of()));

            return reviewsMono.map(reviewsResp -> {
                Object reviewsData = reviewsResp.getOrDefault("data", Map.of());
                Map<String, Map<String, Object>> summaryByProduct = new HashMap<>();
                
                if (reviewsData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> reviewsMap = (Map<String, Object>) reviewsData;
                    for (Map.Entry<String, Object> entry : reviewsMap.entrySet()) {
                        if (entry.getValue() instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> summary = (Map<String, Object>) entry.getValue();
                            summaryByProduct.put(entry.getKey(), summary);
                        }
                    }
                }
                
                List<ProductAggregateDto> aggregatedProducts = items.stream().map(p -> {
                    // Map uuid to id (ProductDto uses uuid, but we use id in DTO)
                    String uuid = Objects.toString(p.get("uuid"), null);
                    if (uuid == null) {
                        uuid = Objects.toString(p.get("id"), null); // Fallback to id if uuid not present
                    }
                    
                    Map<String, Object> summary = summaryByProduct.getOrDefault(uuid, Map.of());
                    Double avg = summary.get("averageRating") instanceof Number n ? ((Number) n).doubleValue() : null;
                    Integer count = summary.get("reviewsCount") instanceof Number n ? ((Number) n).intValue() : null;
                    
                    // Extract rate from product itself if available, otherwise use review summary
                    if (avg == null && p.get("rate") instanceof Number) {
                        Number rateNum = (Number) p.get("rate");
                        avg = rateNum.doubleValue();
                    }
                    
                    // Safely extract tags - ProductDto has List<TagSummaryDto> with uuid and name
                    List<String> tags = List.of();
                    Object tagsObj = p.getOrDefault("tags", null);
                    if (tagsObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> rawTags = (List<Object>) tagsObj;
                        tags = rawTags.stream()
                            .map(tag -> {
                                if (tag instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> tagMap = (Map<String, Object>) tag;
                                    // Extract name from TagSummaryDto
                                    return Objects.toString(tagMap.getOrDefault("name", tagMap.get("uuid")), null);
                                }
                                return Objects.toString(tag, null);
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    }
                    
                    // Map image to imageUrl (ProductDto uses image, but we use imageUrl in DTO)
                    String imageUrl = Objects.toString(p.get("image"), null);
                    if (imageUrl == null) {
                        imageUrl = Objects.toString(p.get("imageUrl"), null); // Fallback
                    }
                    
                    return ProductAggregateDto.builder()
                        .id(uuid)
                        .name(Objects.toString(p.get("name"), null))
                        .price(parseBigDecimal(p.get("price")))
                        .imageUrl(imageUrl)
                        .averageRating(avg)
                        .reviewsCount(count)
                        .tags(tags)
                        .build();
                }).collect(Collectors.toList());
                
                // Extract pagination information from ProductPageDto
                int currentPage = pageData != null && pageData.get("currentPage") instanceof Number 
                    ? ((Number) pageData.get("currentPage")).intValue() : 0;
                int totalPages = pageData != null && pageData.get("totalPages") instanceof Number 
                    ? ((Number) pageData.get("totalPages")).intValue() : 1;
                long totalElements = pageData != null && pageData.get("totalElements") instanceof Number 
                    ? ((Number) pageData.get("totalElements")).longValue() : items.size();
                int pageSize = pageData != null && pageData.get("pageSize") instanceof Number 
                    ? ((Number) pageData.get("pageSize")).intValue() : items.size();
                boolean hasNext = pageData != null && pageData.get("hasNext") instanceof Boolean 
                    ? (Boolean) pageData.get("hasNext") : false;
                boolean hasPrevious = pageData != null && pageData.get("hasPrevious") instanceof Boolean 
                    ? (Boolean) pageData.get("hasPrevious") : false;
                boolean isFirst = pageData != null && pageData.get("isFirst") instanceof Boolean 
                    ? (Boolean) pageData.get("isFirst") : currentPage == 0;
                boolean isLast = pageData != null && pageData.get("isLast") instanceof Boolean 
                    ? (Boolean) pageData.get("isLast") : !hasNext;
                
                return ProductPageDto.builder()
                    .products(aggregatedProducts)
                    .currentPage(currentPage)
                    .totalPages(totalPages)
                    .totalElements(totalElements)
                    .pageSize(pageSize)
                    .hasNext(hasNext)
                    .hasPrevious(hasPrevious)
                    .isFirst(isFirst)
                    .isLast(isLast)
                    .build();
            });
        });
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal b) return b;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }
}

