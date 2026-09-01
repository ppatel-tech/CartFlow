package com.cartflow.order.repository;

import com.cartflow.order.entity.Order;
import com.cartflow.order.entity.OrderStatus;
import com.cartflow.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByIdAndCustomer(Long id, User customer);
    Page<Order> findByCustomer(User customer, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.paymentStatus = 'SUCCESS'")
    BigDecimal getTotalRevenue();

    long countByOrderStatusIn(java.util.List<com.cartflow.order.entity.OrderStatus> statuses);

    @Query("SELECT COALESCE(SUM(o.tax), 0) FROM Order o WHERE o.paymentStatus = 'SUCCESS'")
    BigDecimal getTotalTaxCollected();

    @Query("SELECT COALESCE(SUM(o.discount), 0) FROM Order o WHERE o.paymentStatus = 'SUCCESS'")
    BigDecimal getTotalDiscountsGiven();

    long countByPaymentStatus(com.cartflow.order.entity.PaymentStatus status);

    long countByOrderStatus(OrderStatus status);

    @Query("SELECT o.customer.id, CONCAT(o.customer.firstName, ' ', o.customer.lastName), " +
            "COUNT(o), SUM(o.finalAmount) " +
            "FROM Order o WHERE o.paymentStatus = 'SUCCESS' " +
            "GROUP BY o.customer.id, o.customer.firstName, o.customer.lastName " +
            "ORDER BY SUM(o.finalAmount) DESC")
    List<Object[]> getCustomerSalesReport();
}