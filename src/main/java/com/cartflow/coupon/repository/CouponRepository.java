package com.cartflow.coupon.repository;

import com.cartflow.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByCode(String code);
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
}