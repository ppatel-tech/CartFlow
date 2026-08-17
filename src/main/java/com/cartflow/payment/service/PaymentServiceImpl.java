package com.cartflow.payment.service;

import com.cartflow.exception.PaymentException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.order.entity.Order;
import com.cartflow.order.entity.OrderStatus;
import com.cartflow.order.entity.PaymentStatus;
import com.cartflow.order.repository.OrderRepository;
import com.cartflow.payment.dto.request.InitiatePaymentRequest;
import com.cartflow.payment.dto.request.VerifyPaymentRequest;
import com.cartflow.payment.dto.response.PaymentResponse;
import com.cartflow.payment.entity.Payment;
import com.cartflow.payment.repository.PaymentRepository;
import com.cartflow.user.entity.User;
import com.cartflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(String email, InitiatePaymentRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findByIdAndCustomer(request.getOrderId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new PaymentException("This order has already been paid for");
        }

        if (order.getOrderStatus() == com.cartflow.order.entity.OrderStatus.CANCELLED) {
            throw new PaymentException("Cannot initiate payment for a cancelled order");
        }

        Payment payment = paymentRepository.findByOrder(order).orElse(null);

        if (payment == null) {
            payment = Payment.builder()
                    .order(order)
                    .paymentMethod(request.getPaymentMethod())
                    .paymentStatus(PaymentStatus.PENDING)
                    .transactionReference(generateTransactionReference())
                    .build();
        } else {
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setPaymentStatus(PaymentStatus.PENDING);
        }

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment initiated for order {}: reference {}",
                order.getOrderNumber(), savedPayment.getTransactionReference());

        return mapToResponse(savedPayment);
    }



    @Override
    @Transactional
    public PaymentResponse verifyPayment(String email, VerifyPaymentRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Payment payment = paymentRepository.findByTransactionReference(request.getTransactionReference())
                .orElseThrow(() -> new PaymentException("Invalid transaction reference"));

        Order order = payment.getOrder();

        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new PaymentException("This payment does not belong to you");
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new PaymentException("This payment has already been processed");
        }

        if (Boolean.TRUE.equals(request.getSuccess())) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPaidAmount(order.getFinalAmount());
            payment.setPaidAt(Instant.now());

            order.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setOrderStatus(OrderStatus.CONFIRMED);

            log.info("Payment succeeded for order {}", order.getOrderNumber());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);

            order.setPaymentStatus(PaymentStatus.FAILED);

            log.info("Payment failed for order {}", order.getOrderNumber());
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        return mapToResponse(payment);
    }

    private String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionReference(payment.getTransactionReference())
                .paidAmount(payment.getPaidAmount())
                .paidAt(payment.getPaidAt())
                .build();
    }
}