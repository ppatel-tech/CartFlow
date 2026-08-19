package com.cartflow.order.repository;

import com.cartflow.order.entity.Order;
import com.cartflow.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
            "WHERE oi.product.id = :productId " +
            "AND oi.order.customer.id = :userId " +
            "AND oi.order.orderStatus = 'DELIVERED'")
    boolean existsDeliveredPurchase(@Param("productId") Long productId, @Param("userId") Long userId);
}