package com.cartflow.coupon.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.coupon.dto.request.ApplyCouponRequest;
import com.cartflow.coupon.dto.request.CouponRequest;
import com.cartflow.coupon.dto.response.CouponDiscountResponse;
import com.cartflow.coupon.dto.response.CouponResponse;
import com.cartflow.coupon.service.CouponService;
import com.cartflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponRequest request) {

        CouponResponse response = couponService.createCoupon(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", response));
    }

    @PutMapping("/{id}")
    public ApiResponse<CouponResponse> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {

        CouponResponse response = couponService.updateCoupon(id, request);
        return ApiResponse.success("Coupon updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ApiResponse.success("Coupon deactivated successfully", null);
    }

    @GetMapping
    public ApiResponse<Page<CouponResponse>> getAllCoupons(@ParameterObject Pageable pageable) {
        Page<CouponResponse> response = couponService.getAllCoupons(pageable);
        return ApiResponse.success("Coupons retrieved successfully", response);
    }

    @PostMapping("/validate")
    public ApiResponse<CouponDiscountResponse> validateCoupon(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String code) {

        CouponDiscountResponse response = couponService.validateCoupon(principal.getEmail(), code);
        return ApiResponse.success("Coupon is valid", response);
    }

    @PostMapping("/apply")
    public ApiResponse<CouponDiscountResponse> applyCoupon(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ApplyCouponRequest request) {

        CouponDiscountResponse response = couponService.applyCoupon(principal.getEmail(), request);
        return ApiResponse.success("Coupon applied successfully", response);
    }
}

