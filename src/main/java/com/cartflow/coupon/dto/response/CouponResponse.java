package com.cartflow.coupon.dto.response;

import com.cartflow.coupon.entity.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class CouponResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumPurchase;
    private BigDecimal maximumDiscount;
    private Integer usageLimit;
    private Integer usedCount;
    private Instant expiryDate;
    private boolean isActive;
}