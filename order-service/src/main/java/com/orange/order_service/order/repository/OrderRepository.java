package com.orange.order_service.order.repository;

import com.orange.order_service.order.entity.Order;
import com.orange.order_service.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.cartId = :cartUuid")
    Optional<Order> findByCartUuidQuery(@Param("cartUuid") UUID cartUuid);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.uuid = :orderId AND o.userId = :userId")
    Optional<Order> findByUuidAndUserId(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

    @Query("SELECT o FROM Order o WHERE o.uuid = :uuid")
    Optional<Order> findByUuid(@Param("uuid") UUID uuid);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByStatusOrderByCreatedAtDesc(@Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    Page<Order> findAllOrderByCreatedAtDesc(Pageable pageable);
}