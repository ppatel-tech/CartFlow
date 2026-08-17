package com.cartflow.payment.repository;

import com.cartflow.order.entity.Order;
import com.cartflow.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByTransactionReference(String transactionReference);
}