package com.orange.product_service.product.repo;

import com.orange.product_service.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAll();

    Optional<Product> findByUuid(UUID uuid);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.uuid = :uuid")
    void incrementViewCount(@Param("uuid") UUID uuid);

    List<Product> findByUuidIn(List<UUID> uuids);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    Page<Product> findActiveProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = true")
    Page<Product> findDeletedProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.uuid = :categoryUuid AND p.isDeleted = false")
    Page<Product> findByCategoryUuid(@Param("categoryUuid") UUID categoryUuid, Pageable pageable);
}
