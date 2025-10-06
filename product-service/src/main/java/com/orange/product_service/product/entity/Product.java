package com.orange.product_service.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orange.product_service.category.entity.Category;
import com.orange.product_service.entity.BaseEntity;
import com.orange.product_service.review.entity.Review;
import com.orange.product_service.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "image", length = 500)
    private String image;
    
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;
    
    @Column(name = "rate", precision = 3, scale = 2)
    private BigDecimal rate = BigDecimal.ZERO;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_tags",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
    
    @Column(name = "variants", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String variants; 

    // Helper methods
    public void addTag(Tag tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(tag);
        if (tag.getProducts() != null) {
            tag.getProducts().add(this);
        }
    }
    
    public void removeTag(Tag tag) {
        if (tags != null) {
            tags.remove(tag);
        }
        if (tag.getProducts() != null) {
            tag.getProducts().remove(this);
        }
    }
    
    public void addReview(Review review) {
        if (reviews == null) {
            reviews = new ArrayList<>();
        }
        reviews.add(review);
        review.setProduct(this);
    }
    
    public void removeReview(Review review) {
        if (reviews != null) {
            reviews.remove(review);
        }
        review.setProduct(null);
    }
    
    public void incrementViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0L;
        }
        this.viewCount++;
    }
    
    // Variant management helper methods
    public boolean hasVariants() {
        return variants != null && !variants.trim().isEmpty();
    }
    
    public void setVariantsJson(String variantsJson) {
        this.variants = variantsJson;
    }
    
    public String getVariantsJson() {
        return this.variants;
    }
}