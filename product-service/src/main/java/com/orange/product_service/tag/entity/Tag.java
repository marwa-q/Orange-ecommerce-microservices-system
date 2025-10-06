package com.orange.product_service.tag.entity;

import com.orange.product_service.entity.BaseEntity;
import com.orange.product_service.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag extends BaseEntity {
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
    
    // Helper methods
    public void addProduct(Product product) {
        if (products == null) {
            products = new ArrayList<>();
        }
        products.add(product);
        if (product.getTags() != null) {
            product.getTags().add(this);
        }
    }
    
    public void removeProduct(Product product) {
        if (products != null) {
            products.remove(product);
        }
        if (product.getTags() != null) {
            product.getTags().remove(this);
        }
    }
}