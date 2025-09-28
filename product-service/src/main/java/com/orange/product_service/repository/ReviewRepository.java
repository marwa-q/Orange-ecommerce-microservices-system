package com.orange.product_service.repository;

import com.orange.product_service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends BaseRepository<Review> {
    
    List<Review> findByProductId(String productId);
    
    List<Review> findByUserId(String userId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.userId = :userId")
    Optional<Review> findByProductIdAndUserId(@Param("productId") String productId, @Param("userId") String userId);
    
    @Query("SELECT AVG(r.rate) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") String productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long getReviewCountByProductId(@Param("productId") String productId);
}
