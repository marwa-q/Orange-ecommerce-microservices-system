package com.orange.cart_service.cart.repo;

import com.orange.cart_service.cartItem.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByUuid(UUID uuid);

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, UUID productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId")
    Optional<CartItem> findByCartIdAndProductIdQuery(@Param("cartId") Long cartId, @Param("productId") UUID productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.uuid = :cartUuid")
    List<CartItem> findByCartUuid(@Param("cartUuid") UUID cartUuid);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.userId = :userId AND ci.cart.status = 'ACTIVE'")
    List<CartItem> findActiveCartItemsByUserId(@Param("userId") UUID userId);

    void deleteByCartId(Long cartId);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    long countByCartId(@Param("cartId") Long cartId);
}
