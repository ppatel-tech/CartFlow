package com.cartflow.order.repository;

import com.cartflow.order.entity.Order;
import com.cartflow.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByIdAndCustomer(Long id, User customer);
    Page<Order> findByCustomer(User customer, Pageable pageable);
}