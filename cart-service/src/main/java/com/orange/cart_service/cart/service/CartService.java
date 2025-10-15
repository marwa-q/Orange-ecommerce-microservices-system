package com.orange.cart_service.cart.service;

import com.orange.cart_service.client.ProductClient;
import com.orange.cart_service.cart.dto.AddCartItemRequest;
import com.orange.cart_service.cart.dto.CartDto;
import com.orange.cart_service.cart.dto.ProductSummaryDto;
import com.orange.cart_service.cart.dto.UpdateQuantityRequest;
import com.orange.cart_service.cart.entity.Cart;
import com.orange.cart_service.cart.event.CartCheckoutEvent;
import com.orange.cart_service.cart.repo.CartRepository;
import com.orange.cart_service.cartItem.entity.CartItem;
import com.orange.cart_service.cartItem.dto.CartItemDto;
import com.orange.cart_service.cartItem.repo.CartItemRepository;
import com.orange.cart_service.common.dto.ApiResponse;

import com.orange.cart_service.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;
    private final JwtUtil jwtUtil;
    private final CartEventPublisher cartEventPublisher;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductClient productClient,
                       JwtUtil jwtUtil,
                       CartEventPublisher cartEventPublisher) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
        this.jwtUtil = jwtUtil;
        this.cartEventPublisher = cartEventPublisher;
    }

    @Transactional
    public ApiResponse<?> addItemToCart(AddCartItemRequest request, UUID userId) {

        // Fetch product
        ApiResponse<ProductSummaryDto> productResponse = productClient.getProductById(request.getProductId());
        if (productResponse == null || !productResponse.isSuccess() || productResponse.getData() == null) {
            return ApiResponse.failure("product.not_found");
        }
        ProductSummaryDto product = productResponse.getData();

        // Validate stock
        if (product.getStock() == null || product.getStock() < request.getQuantity()) {
            return ApiResponse.failure("cart.insufficient_stock");
        }

        // Find or create cart
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(userId);
                    return cartRepository.save(c);
                });

        // Load existing cart items to ensure we have the latest data
        List<CartItem> existingItems = cartItemRepository.findByCartId(cart.getId());
        
        // Find existing item by product ID
        Optional<CartItem> existingItemOpt = existingItems.stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();
        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();

            int newQty = item.getQuantity() + request.getQuantity();
            if (product.getStock() < newQty) {
                return ApiResponse.failure("cart.insufficient_stock");
            }

            item.setQuantity(newQty);
            item.calculateSubtotal();
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(request.getProductId());
            item.setPrice(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
            item.setQuantity(request.getQuantity());
            item.calculateSubtotal();

            cartItemRepository.save(item);
        }

        // Recalculate total amount based on all items
        List<CartItem> allItems = cartItemRepository.findByCartId(cart.getId());
        BigDecimal totalAmount = allItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(totalAmount);
        
        cartRepository.save(cart);
        return ApiResponse.success("cart.item_added");
    }

    public ApiResponse<List<CartItemDto>> getActiveCartItems(UUID userId) {
        List<CartItem> cartItems = cartItemRepository.findActiveCartItemsByUserId(userId);
        List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(cartItemDtos);
    }

    public ApiResponse<List<CartItemDto>> getCartItemsByCartId(UUID cartId) {
        List<CartItem> cartItems = cartItemRepository.findByCartUuid(cartId);
        List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(cartItemDtos);
    }

    public ApiResponse<CartDto> getCartInfo(UUID userId) {
        // Find active cart for user
        Optional<Cart> cartOpt = cartRepository.findActiveCartByUserId(userId);
        if (cartOpt.isEmpty()) {
            return ApiResponse.failure("cart.not_found");
        }
        
        Cart cart = cartOpt.get();

        // Explicitly load cart items and recalculate total
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        cart.setCartItems(cartItems);
        cart.calculateTotalAmount();
        
        CartDto cartDto = convertCartToDto(cart);
        
        return ApiResponse.success(cartDto);
    }

    @Transactional
    public ApiResponse<Void> removeItemFromCart(UUID productId, UUID userId) {
        // Find active cart for user
        Optional<Cart> cartOpt = cartRepository.findActiveCartByUserId(userId);
        if (cartOpt.isEmpty()) {
            return ApiResponse.failure("cart.not_found");
        }
        
        Cart cart = cartOpt.get();
        
        // Find cart item by product ID
        Optional<CartItem> itemOpt = cartItemRepository.findByCartUuidAndProductId(cart.getUuid(), productId);
        if (itemOpt.isEmpty()) {
            return ApiResponse.failure("cart.item_not_found");
        }
        
        CartItem item = itemOpt.get();
        
        // Remove item from cart
        cart.removeCartItem(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);
        
        return ApiResponse.success("cart.item_removed");
    }


    @Transactional
    public ApiResponse<?> updateItemQuantity(UUID productId, UpdateQuantityRequest request, UUID userId) {
        try {
            // Validate request
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                return ApiResponse.failure("cart.invalid_quantity");
            }

            // Find the user's active cart
            Optional<Cart> cartOpt = cartRepository.findActiveCartByUserId(userId);
            if (cartOpt.isEmpty()) {
                return ApiResponse.failure("cart.not_found");
            }

            Cart cart = cartOpt.get();

            // Find the cart item by product ID
            List<CartItem> existingItems = cartItemRepository.findByCartId(cart.getId());
            Optional<CartItem> itemOpt = existingItems.stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst();

            if (itemOpt.isEmpty()) {
                return ApiResponse.failure("cart.item_not_found");
            }

            CartItem item = itemOpt.get();

            // Validate stock availability
            ApiResponse<ProductSummaryDto> productResponse = productClient.getProductById(productId);
            if (!productResponse.isSuccess() || productResponse.getData() == null) {
                return ApiResponse.failure("product.not_found");
            }

            ProductSummaryDto product = productResponse.getData();
            if (product.getStock() < request.getQuantity()) {
                return ApiResponse.failure("cart.insufficient_stock");
            }

            // Update quantity
            item.setQuantity(request.getQuantity());
            item.calculateSubtotal();
            cartItemRepository.save(item);

            // Recalculate cart total

            cart.calculateTotalAmount();
            cartRepository.save(cart);

            return ApiResponse.success("cart.item.quantity.updated");

        } catch (Exception e) {
            return ApiResponse.failure("Something went wrong : " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<Void> clearCart(UUID userId) {
        try {
            // Find active cart for user
            Optional<Cart> cartOpt = cartRepository.findActiveCartByUserId(userId);
            if (cartOpt.isEmpty()) {
                return ApiResponse.failure("cart.not_found");
            }
            
            Cart cart = cartOpt.get();
            
            // Get all cart items for this cart
            List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
            
            // Delete all cart items directly from the repository
            // This avoids the orphanRemoval issue by not touching the collection
            if (!cartItems.isEmpty()) {
                cartItemRepository.deleteAll(cartItems);
            }
            
            // Reset cart total amount to zero
            cart.setTotalAmount(BigDecimal.ZERO);
            
            // Save the cart without touching the cartItems collection
            cartRepository.save(cart);
            
            return ApiResponse.success("cart.cleared");
            
        } catch (Exception e) {
            return ApiResponse.failure("Something went wrong : " + e.getMessage());
        }
    }

    private CartItemDto convertToDto(CartItem cartItem) {
        CartItemDto dto = new CartItemDto();
        dto.setUuid(cartItem.getUuid());
        dto.setProductId(cartItem.getProductId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPrice(cartItem.getPrice());
        dto.setSubtotal(cartItem.getSubtotal());
        dto.setCreatedAt(cartItem.getCreatedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());
        return dto;
    }

    private CartDto convertCartToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setUuid(cart.getUuid());
        dto.setUserId(cart.getUserId());
        dto.setStatus(cart.getStatus());
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        dto.setExpiredAt(cart.getExpiredAt());
        
        // Convert cart items to DTOs
        List<CartItemDto> cartItemDtos = cart.getCartItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        dto.setCartItems(cartItemDtos);
        
        return dto;
    }

    @Transactional
    public ApiResponse<?> checkoutCart(UUID userId) {
        try {
            // Find active cart for user
            Optional<Cart> cartOpt = cartRepository.findActiveCartByUserId(userId);
            if (cartOpt.isEmpty()) {
                return ApiResponse.failure("cart.not_found");
            }
            
            Cart cart = cartOpt.get();
            
            // Check if cart is empty
            if (cart.isEmpty()) {
                return ApiResponse.failure("cart.empty");
            }
            
            // Recalculate total amount to ensure accuracy
            cart.calculateTotalAmount();
            cartRepository.save(cart);
            
            // Create checkout event with cart_id, user_id and totalAmount
            CartCheckoutEvent checkoutEvent = new CartCheckoutEvent(cart.getUuid(), userId, cart.getTotalAmount());
            
            // Publish the event to RabbitMQ
            cartEventPublisher.publishCartCheckoutEvent(checkoutEvent);
            
            // Update cart status to CHECKED_OUT
            cart.markAsCheckedOut();
            cartRepository.save(cart);
            
            return ApiResponse.success("cart.checked.out");
            
        } catch (Exception e) {
            return ApiResponse.failure("Something went wrong during checkout: " + e.getMessage());
        }
    }
}