package com.cartflow.coupon.service;

import com.cartflow.coupon.dto.request.ApplyCouponRequest;
import com.cartflow.coupon.dto.request.CouponRequest;
import com.cartflow.coupon.dto.response.CouponDiscountResponse;
import com.cartflow.coupon.dto.response.CouponResponse;
import com.cartflow.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CouponService {
    CouponResponse createCoupon(CouponRequest request);
    CouponResponse updateCoupon(Long id, CouponRequest request);
    void deleteCoupon(Long id);
    Page<CouponResponse> getAllCoupons(Pageable pageable);
    BigDecimal validateAndCalculateDiscount(String code, BigDecimal cartTotal, User user);
    CouponDiscountResponse validateCoupon(String email, String code);
    CouponDiscountResponse applyCoupon(String email, ApplyCouponRequest request);
    void recordCouponUsage(String code, User user);
}