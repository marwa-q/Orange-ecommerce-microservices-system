package com.orange.gateway_service.bff.dto.product;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAggregateDto {
        private String id;
        private String name;
        private CategorySummaryDto category;
        private BigDecimal price;
        private String imageUrl;
        private Integer stock;
        private Boolean isDeleted;
        private Long viewCount;
        private BigDecimal rate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<TagSummaryDto> tags;
        private List<ReviewSummaryDto> reviews;
        private List<ProductVariant> variants;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CategorySummaryDto {
            private UUID uuid;
            private String name;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TagSummaryDto {
            private UUID uuid;
            private String name;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ReviewSummaryDto {
            private UUID uuid;
            private UUID userId;
            private BigDecimal rate;
        }
    }




