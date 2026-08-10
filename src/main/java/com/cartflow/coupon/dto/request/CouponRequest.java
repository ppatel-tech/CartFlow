package com.cartflow.coupon.dto.request;

import com.cartflow.coupon.entity.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Pattern(regexp = "^[A-Z0-9]{4,30}$",
            message = "Coupon code must be 4-30 uppercase letters/digits")
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be positive")
    private BigDecimal discountValue;

    @NotNull(message = "Minimum purchase amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum purchase cannot be negative")
    private BigDecimal minimumPurchase;

    private BigDecimal maximumDiscount;

    @NotNull(message = "Usage limit is required")
    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private Instant expiryDate;
}