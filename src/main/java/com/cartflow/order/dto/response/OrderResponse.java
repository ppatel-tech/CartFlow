package com.cartflow.order.dto.response;

import com.cartflow.order.entity.OrderStatus;
import com.cartflow.order.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private List<OrderItemResponse> items;

    private String shippingFullName;
    private String shippingPhone;
    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private String shippingCountry;
    private String shippingPostalCode;

    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal shippingCharge;
    private BigDecimal finalAmount;
    private String couponCode;

    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private Instant createdAt;
}