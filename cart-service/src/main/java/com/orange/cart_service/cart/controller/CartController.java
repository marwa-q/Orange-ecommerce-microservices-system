package com.orange.cart_service.cart.controller;

import com.orange.cart_service.cart.dto.AddCartItemRequest;
import com.orange.cart_service.cart.dto.CartDto;
import com.orange.cart_service.cart.dto.UpdateQuantityRequest;
import com.orange.cart_service.cart.service.CartService;
import com.orange.cart_service.cart.service.CartCleanupService;
import com.orange.cart_service.cartItem.dto.CartItemDto;
import com.orange.cart_service.common.dto.ApiResponse;
import com.orange.cart_service.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart Controller", description = "Cart management endpoints")
public class CartController {

    private final CartService cartService;
    private final CartCleanupService cartCleanupService;
    private final JwtUtil jwt;

    public CartController(CartService cartService, CartCleanupService cartCleanupService, JwtUtil jwt) {
        this.cartService = cartService;
        this.cartCleanupService = cartCleanupService;
        this.jwt = jwt;
    }

    @PostMapping("/items/add")
    @Operation(summary = "Add item to cart", description = "Add a product with quantity to the user's cart")
    public ResponseEntity<ApiResponse<?>> addItem(
            @Valid @RequestBody AddCartItemRequest request,
            HttpServletRequest httpServletRequest
    ) {
        UUID userId = getCurrentUserId(httpServletRequest);
        ApiResponse<?> response = cartService.addItemToCart(request, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping
    @Operation(summary = "Get cart info", description = "Get current user's cart information with items")
    public ResponseEntity<ApiResponse<CartDto>> getCart(
            HttpServletRequest httpServletRequest
    ) {
        UUID userId = getCurrentUserId(httpServletRequest);
        ApiResponse<CartDto> response = cartService.getCartInfo(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/items")
    @Operation(summary = "Get cart items", description = "Get current user's active cart items")
    public ResponseEntity<ApiResponse<List<CartItemDto>>> getCartItems(
            HttpServletRequest httpServletRequest
    ) {
        UUID userId = getCurrentUserId(httpServletRequest);
        ApiResponse<List<CartItemDto>> response = cartService.getActiveCartItems(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/{cartId}/items")
    @Operation(summary = "Get cart items by cart ID", description = "Get cart items for a specific cart ID")
    public ResponseEntity<ApiResponse<List<CartItemDto>>> getCartItemsByCartId(
            @PathVariable UUID cartId,
            HttpServletRequest httpServletRequest
    ) {
        ApiResponse<List<CartItemDto>> response = cartService.getCartItemsByCartId(cartId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Update item quantity", description = "Update the quantity of a product in the user's cart")
    public ResponseEntity<ApiResponse<?>> updateItemQuantity(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateQuantityRequest request,
            HttpServletRequest httpServletRequest
    ) {
        UUID userId = getCurrentUserId(httpServletRequest);
        ApiResponse<?> response = cartService.updateItemQuantity(productId, request, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart", description = "Remove a product from the user's cart by product ID")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable UUID productId,
            HttpServletRequest httpServletRequest
    ) {
        UUID userId = getCurrentUserId(httpServletRequest);
        ApiResponse<Void> response = cartService.removeItemFromCart(productId, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Clear cart", description = "Clear all items from the current user's cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            HttpServletRequest httpServletRequest
    ) {
        UUID userId = getCurrentUserId(httpServletRequest);
        ApiResponse<Void> response = cartService.clearCart(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/checkout")
    @Operation(summary = "Checkout cart", description = "Convert cart to order and trigger OrderService by publishing CartCheckoutEvent")
    public ResponseEntity<ApiResponse<?>> checkoutCart(
            HttpServletRequest httpServletRequest
    ) {
        UUID userId = getCurrentUserId(httpServletRequest);
        ApiResponse<?> response = cartService.checkoutCart(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }


    @PostMapping("/cleanup")
    @Operation(summary = "Manual cart cleanup", description = "Manually trigger cleanup of expired carts (Admin only)")
    public ResponseEntity<ApiResponse<Integer>> manualCleanup() {
        int cleanedCount = cartCleanupService.manualCleanupExpiredCarts();
        return ResponseEntity.ok(ApiResponse.success(cleanedCount));
    }

    // Function to extract user uuid from token
    public UUID getCurrentUserId(HttpServletRequest request) {
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