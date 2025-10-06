package com.orange.product_service.review.entity;

import com.orange.product_service.entity.BaseEntity;
import com.orange.product_service.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "uuid", nullable = false)
    private Product product;
    
    @Column(name = "rate", nullable = false, precision = 3, scale = 2)
    private BigDecimal rate;
}