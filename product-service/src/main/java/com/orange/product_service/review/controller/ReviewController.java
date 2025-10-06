package com.orange.product_service.review.controller;

import com.orange.product_service.dto.ApiResponse;
import com.orange.product_service.review.dto.CreateReviewRequest;
import com.orange.product_service.review.dto.DeleteReviewRequest;
import com.orange.product_service.review.dto.ReviewDto;
import com.orange.product_service.review.dto.ReviewStatisticsDto;
import com.orange.product_service.review.dto.UpdateReviewRequest;
import com.orange.product_service.review.service.ReviewService;
import com.orange.product_service.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Controller", description = "Product review management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwt;

    public ReviewController(ReviewService reviewService, JwtUtil jwt) {
        this.reviewService = reviewService;
        this.jwt = jwt;
    }

    @PostMapping("/create")
    @Operation(summary = "Create product review", description = "Create a new product review (Authenticated users only)")
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language,
            HttpServletRequest httpRequest) {

        Locale locale = Locale.forLanguageTag(language);
        UUID userId = getCurrentUserId(httpRequest);
        ApiResponse<ReviewDto> response = reviewService.createReview(request, userId, locale);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update")
    @Operation(summary = "Update product review", description = "Update an existing product review (Owner only)")
    public ResponseEntity<ApiResponse<ReviewDto>> updateReview(
            @Valid @RequestBody UpdateReviewRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language,
            HttpServletRequest httpRequest) {

        Locale locale = Locale.forLanguageTag(language);
        UUID userId = getCurrentUserId(httpRequest);
        ApiResponse<ReviewDto> response = reviewService.updateReview(request, userId, locale);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/delete")
    @Operation(summary = "Delete product review", description = "Delete a product review (Owner only)")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Valid @RequestBody DeleteReviewRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language,
            HttpServletRequest httpRequest) {

        Locale locale = Locale.forLanguageTag(language);
        UUID userId = getCurrentUserId(httpRequest);
        ApiResponse<Void> response = reviewService.deleteReview(request, userId, locale);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product reviews", description = "Get all reviews for a specific product (Public endpoint)")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getReviewsByProduct(
            @PathVariable UUID productId,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<List<ReviewDto>> response = reviewService.getReviewsByProduct(productId, locale);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user")
    @Operation(summary = "Get user reviews", description = "Get all reviews by the current user (Authenticated users only)")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getReviewsByUser(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language,
            HttpServletRequest httpRequest) {

        Locale locale = Locale.forLanguageTag(language);
        UUID userId = getCurrentUserId(httpRequest);
        ApiResponse<List<ReviewDto>> response = reviewService.getReviewsByUser(userId, locale);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review details", description = "Get review details by ID (Public endpoint)")
    public ResponseEntity<ApiResponse<ReviewDto>> getReviewDetails(
            @PathVariable UUID reviewId,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ReviewDto> response = reviewService.getReviewById(reviewId, locale);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/statistics/{productId}")
    @Operation(summary = "Get review statistics", description = "Get review statistics for a specific product (Public endpoint)")
    public ResponseEntity<ApiResponse<ReviewStatisticsDto>> getReviewStatistics(
            @PathVariable UUID productId,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ReviewStatisticsDto> response = reviewService.getReviewStatistics(productId, locale);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Function to extract user uuid from token
    private UUID getCurrentUserId(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                UUID userId = jwt.getUserId(token);
                if (userId != null) {
                    return userId;
                }
            }
            throw new RuntimeException("Unable to extract user ID from token");
        } catch (Exception e) {
            throw new RuntimeException("User not authenticated: " + e.getMessage());
        }
    }
}
