package com.orange.cart_service.cart.repo;

import com.orange.cart_service.cart.entity.Cart;
import com.orange.cart_service.cart.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUuid(UUID uuid);

    List<Cart> findByUserId(UUID userId);

    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.status = 'ACTIVE'")
    Optional<Cart> findActiveCartByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM Cart c WHERE c.expiredAt < :now AND c.status = 'ACTIVE'")
    List<Cart> findExpiredCarts(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Cart c WHERE c.status = :status")
    List<Cart> findByStatus(@Param("status") CartStatus status);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.userId = :userId AND c.status = 'ACTIVE'")
    long countActiveCartsByUserId(@Param("userId") UUID userId);
}
