package com.orange.cart_service.cart.service;

import com.orange.cart_service.cart.entity.Cart;
import com.orange.cart_service.cart.repo.CartRepository;
import com.orange.cart_service.cartItem.repo.CartItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CartCleanupService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartCleanupService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * Scheduled job to clean up expired carts every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredCarts() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Cart> expiredCarts = cartRepository.findExpiredCarts(now);
            
            if (expiredCarts.isEmpty()) {
                logger.debug("No expired carts found for cleanup");
                return;
            }

            logger.info("Found {} expired carts to clean up", expiredCarts.size());
            
            int cleanedCount = 0;
            for (Cart cart : expiredCarts) {
                try {
                    // Delete cart items first (due to foreign key constraints)
                    cartItemRepository.deleteByCartId(cart.getId());
                    
                    // Delete the cart
                    cartRepository.delete(cart);
                    
                    cleanedCount++;
                    logger.debug("Cleaned up expired cart: {} for user: {}", 
                            cart.getUuid(), cart.getUserId());
                } catch (Exception e) {
                    logger.error("Failed to clean up cart: {} - Error: {}", 
                            cart.getUuid(), e.getMessage());
                }
            }
            
            logger.info("Successfully cleaned up {} expired carts", cleanedCount);
            
        } catch (Exception e) {
            logger.error("Error during cart cleanup process: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual cleanup method for testing or immediate cleanup
     */
    @Transactional
    public int manualCleanupExpiredCarts() {
        LocalDateTime now = LocalDateTime.now();
        List<Cart> expiredCarts = cartRepository.findExpiredCarts(now);
        
        int cleanedCount = 0;
        for (Cart cart : expiredCarts) {
            try {
                cartItemRepository.deleteByCartId(cart.getId());
                cartRepository.delete(cart);
                cleanedCount++;
            } catch (Exception e) {
                logger.error("Failed to clean up cart: {} - Error: {}", 
                        cart.getUuid(), e.getMessage());
            }
        }
        
        logger.info("Manual cleanup completed. Cleaned up {} expired carts", cleanedCount);
        return cleanedCount;
    }
}
