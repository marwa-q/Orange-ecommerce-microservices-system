package com.orange.product_service.review.service;

import com.orange.product_service.dto.ApiResponse;
import com.orange.product_service.product.entity.Product;
import com.orange.product_service.product.repo.ProductRepository;
import com.orange.product_service.review.dto.CreateReviewRequest;
import com.orange.product_service.review.dto.DeleteReviewRequest;
import com.orange.product_service.review.dto.ReviewDto;
import com.orange.product_service.review.dto.ReviewStatisticsDto;
import com.orange.product_service.review.dto.UpdateReviewRequest;
import com.orange.product_service.review.entity.Review;
import com.orange.product_service.review.repo.ReviewRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final MessageSource messageSource;

    public ReviewService(ReviewRepository reviewRepository, 
                        ProductRepository productRepository, 
                        MessageSource messageSource) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.messageSource = messageSource;
    }

    // Create review
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory", "productDetails", "reviews", "reviewStatistics"}, allEntries = true)
    public ApiResponse<ReviewDto> createReview(CreateReviewRequest request, UUID userId, Locale locale) {
        try {
            // Check if product exists
            Product product = productRepository.findByUuid(request.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.productId()));

            // Check if user already reviewed this product
            if (reviewRepository.findByUserIdAndProductUuid(userId, request.productId()).isPresent()) {
                String msg = messageSource.getMessage("review.create.duplicate", null, locale);
                return ApiResponse.failure(msg);
            }
            
            Review review = new Review();
            review.setUserId(userId);
            review.setProduct(product);
            review.setRate(request.rate());
            
            Review savedReview = reviewRepository.save(review);
            
            // Update product average rating
            updateProductAverageRating(request.productId());
            
            ReviewDto reviewDto = convertToDto(savedReview);
            String msg = messageSource.getMessage("review.created.success", null, locale);
            return ApiResponse.success(msg, reviewDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("review.created.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Update review
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory", "productDetails", "reviews", "reviewStatistics"}, allEntries = true)
    public ApiResponse<ReviewDto> updateReview(UpdateReviewRequest request, UUID userId, Locale locale) {
        try {
            Review review = reviewRepository.findByUserIdAndProductUuid(userId, request.productId())
                    .orElseThrow(() -> new RuntimeException("Review not found with ID: " + request.productId()));

            // Check if user owns this review
            if (!review.getUserId().equals(userId)) {
                String msg = messageSource.getMessage("review.update.unauthorized", null, locale);
                return ApiResponse.failure(msg);
            }
            
            review.setRate(request.rate());
            Review savedReview = reviewRepository.save(review);
            
            // Update product average rating
            updateProductAverageRating(review.getProduct().getUuid());
            
            ReviewDto reviewDto = convertToDto(savedReview);
            String msg = messageSource.getMessage("review.updated.success", null, locale);
            return ApiResponse.success(msg, reviewDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("review.updated.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Delete review
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory", "productDetails", "reviews", "reviewStatistics"}, allEntries = true)
    public ApiResponse<Void> deleteReview(DeleteReviewRequest request, UUID userId, Locale locale) {
        try {
            Review review = reviewRepository.findByUuid(request.reviewId())
                    .orElseThrow(() -> new RuntimeException("Review not found with ID: " + request.reviewId()));
            
            // Check if user owns this review
            if (!review.getUserId().equals(userId)) {
                String msg = messageSource.getMessage("review.delete.unauthorized", null, locale);
                return ApiResponse.failure(msg);
            }
            
            UUID productUuid = review.getProduct().getUuid();
            reviewRepository.delete(review);
            
            // Update product average rating
            updateProductAverageRating(productUuid);
            
            String msg = messageSource.getMessage("review.deleted.success", null, locale);
            return ApiResponse.success(msg, null);
        } catch (Exception e) {
            String msg = messageSource.getMessage("review.deleted.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Get reviews by product
    @Transactional(readOnly = true)
    @Cacheable(value = "reviews", key = "'product_' + #productId")
    public ApiResponse<List<ReviewDto>> getReviewsByProduct(UUID productId, Locale locale) {
        try {
            List<Review> reviews = reviewRepository.findByProductUuidOrderByCreatedAtDesc(productId);
            List<ReviewDto> reviewDtos = reviews.stream()
                    .map(this::convertToDto)
                    .toList();
            
            String msg = messageSource.getMessage("review.list.success", null, locale);
            return ApiResponse.success(msg, reviewDtos);
        } catch (Exception e) {
            String msg = messageSource.getMessage("review.list.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Get reviews by user
    @Transactional(readOnly = true)
    @Cacheable(value = "reviews", key = "'user_' + #userId")
    public ApiResponse<List<ReviewDto>> getReviewsByUser(UUID userId, Locale locale) {
        try {
            List<Review> reviews = reviewRepository.findByUserId(userId);
            List<ReviewDto> reviewDtos = reviews.stream()
                    .map(this::convertToDto)
                    .toList();
            
            String msg = messageSource.getMessage("review.user.list.success", null, locale);
            return ApiResponse.success(msg, reviewDtos);
        } catch (Exception e) {
            String msg = messageSource.getMessage("review.user.list.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Get review by ID
    @Transactional(readOnly = true)
    @Cacheable(value = "reviews", key = "'review_' + #reviewId")
    public ApiResponse<ReviewDto> getReviewById(UUID reviewId, Locale locale) {
        try {
            Review review = reviewRepository.findByUuid(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
            
            ReviewDto reviewDto = convertToDto(review);
            String msg = messageSource.getMessage("review.details.success", null, locale);
            return ApiResponse.success(msg, reviewDto);
        } catch (RuntimeException e) {
            String msg = messageSource.getMessage("review.details.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        } catch (Exception e) {
            String msg = messageSource.getMessage("review.details.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Get review statistics for a product
    @Transactional(readOnly = true)
    @Cacheable(value = "reviewStatistics", key = "#productId")
    public ApiResponse<ReviewStatisticsDto> getReviewStatistics(UUID productId, Locale locale) {
        try {
            // First check if the product exists
            Product product = productRepository.findByUuid(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
            
            // Use safe method that always returns a result
            Object[] stats = reviewRepository.getReviewStatisticsSafe(productId);
            
            // Debug logging
            System.out.println("Debug - Product ID: " + productId);
            System.out.println("Debug - Stats result: " + (stats != null ? java.util.Arrays.toString(stats) : "null"));
            
            // This should always return a result now, but let's keep the safety check
            if (stats == null || stats.length < 4) {
                // Fallback: Return statistics with zero values for products with no reviews
                ReviewStatisticsDto statisticsDto = new ReviewStatisticsDto(
                    productId,
                    0L,  // No reviews
                    BigDecimal.ZERO,  // No average rating
                    BigDecimal.ZERO,  // No min rating
                    BigDecimal.ZERO   // No max rating
                );
                
                String msg = messageSource.getMessage("review.statistics.success", null, locale);
                return ApiResponse.success(msg, statisticsDto);
            }
            
            Long reviewCount = (Long) stats[0];
            Double averageRating = (Double) stats[1];
            BigDecimal minRating = (BigDecimal) stats[2];
            BigDecimal maxRating = (BigDecimal) stats[3];
            
            // Handle null values properly
            if (reviewCount == null) reviewCount = 0L;
            if (averageRating == null) averageRating = 0.0;
            if (minRating == null) minRating = BigDecimal.ZERO;
            if (maxRating == null) maxRating = BigDecimal.ZERO;
            
            ReviewStatisticsDto statisticsDto = new ReviewStatisticsDto(
                productId,
                reviewCount,
                BigDecimal.valueOf(averageRating).setScale(2, java.math.RoundingMode.HALF_UP),
                minRating,
                maxRating
            );
            
            String msg = messageSource.getMessage("review.statistics.success", null, locale);
            return ApiResponse.success(msg, statisticsDto);
            
        } catch (Exception e) {
            String msg = messageSource.getMessage("review.statistics.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Update product average rating with race condition prevention
    @Transactional
    private void updateProductAverageRating(UUID productUuid) {
        try {
            // Use pessimistic locking to prevent race conditions
            Optional<Product> productOpt = reviewRepository.findProductByUuidWithLock(productUuid);
            
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                
                // Calculate the new average rating with proper precision
                Double averageRating = reviewRepository.calculateAverageRatingByProductUuid(productUuid);
                
                if (averageRating != null) {
                    // Round to 2 decimal places for better precision
                    BigDecimal roundedRating = BigDecimal.valueOf(averageRating)
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                    product.setRate(roundedRating);
                } else {
                    product.setRate(BigDecimal.ZERO);
                }
                
                productRepository.save(product);
                
                System.out.println("Updated product " + productUuid + " average rating to: " + product.getRate());
            } else {
                System.err.println("Product not found for UUID: " + productUuid);
            }
        } catch (Exception e) {
            System.err.println("Failed to update product average rating for " + productUuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setUuid(review.getUuid());
        dto.setUserId(review.getUserId());
        dto.setProductId(review.getProduct().getUuid());
        dto.setProductName(review.getProduct().getName());
        dto.setRate(review.getRate());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}
