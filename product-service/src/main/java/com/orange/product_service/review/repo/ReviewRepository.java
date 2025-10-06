package com.orange.product_service.review.repo;

import com.orange.product_service.product.entity.Product;
import com.orange.product_service.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Optional<Review> findByUuid(UUID uuid);
    
    List<Review> findByProductUuid(UUID productUuid);
    
    List<Review> findByUserId(UUID userId);
    
    Optional<Review> findByUserIdAndProductUuid(UUID userId, UUID productUuid);

    @Query("SELECT r FROM Review r WHERE r.product.uuid = :productUuid ORDER BY r.createdAt DESC")
    List<Review> findByProductUuidOrderByCreatedAtDesc(@Param("productUuid") UUID productUuid);
    
    // Method with pessimistic locking to prevent race conditions
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT p FROM Product p WHERE p.uuid = :productUuid")
    Optional<Product> findProductByUuidWithLock(@Param("productUuid") UUID productUuid);
    
    // Calculate average rating with proper precision
    @Query("SELECT COALESCE(AVG(CAST(r.rate AS double)), 0.0) FROM Review r WHERE r.product.uuid = :productUuid")
    Double calculateAverageRatingByProductUuid(@Param("productUuid") UUID productUuid);
    
    // Get review statistics for a product
    @Query("SELECT COUNT(r), COALESCE(AVG(CAST(r.rate AS double)), 0.0), MIN(r.rate), MAX(r.rate) FROM Review r WHERE r.product.uuid = :productUuid")
    Object[] getReviewStatisticsByProductUuid(@Param("productUuid") UUID productUuid);
    
    // Alternative method that always returns a result, even for products with no reviews
    @Query("SELECT " +
           "COALESCE(COUNT(r), 0), " +
           "COALESCE(AVG(CAST(r.rate AS double)), 0.0), " +
           "COALESCE(MIN(r.rate), 0), " +
           "COALESCE(MAX(r.rate), 0) " +
           "FROM Review r WHERE r.product.uuid = :productUuid")
    Object[] getReviewStatisticsSafe(@Param("productUuid") UUID productUuid);
}
