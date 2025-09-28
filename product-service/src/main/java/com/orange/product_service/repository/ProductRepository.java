package com.orange.product_service.repository;

import com.orange.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends BaseRepository<Product> {
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    Page<Product> findActiveProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isDeleted = false")
    Page<Product> findByCategoryId(@Param("categoryId") String categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.isDeleted = false")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isDeleted = false")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.stock <= :threshold AND p.isDeleted = false")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false ORDER BY p.viewCount DESC")
    Page<Product> findMostViewedProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Product> findNewestProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false ORDER BY p.rate DESC")
    Page<Product> findTopRatedProducts(Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isDeleted = false")
    long countActiveProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isDeleted = false")
    long countByCategoryId(@Param("categoryId") String categoryId);
}
