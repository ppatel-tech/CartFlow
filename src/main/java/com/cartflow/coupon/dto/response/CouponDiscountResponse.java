package com.cartflow.coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class CouponDiscountResponse {
    private String code;
    private BigDecimal cartTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
}