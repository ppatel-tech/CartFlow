package com.cartflow.coupon.repository;

import com.cartflow.coupon.entity.Coupon;
import com.cartflow.coupon.entity.CouponUsage;
import com.cartflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    boolean existsByCouponAndUser(Coupon coupon, User user);
}