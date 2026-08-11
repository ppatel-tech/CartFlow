package com.cartflow.coupon.service;

import com.cartflow.cart.service.CartService;
import com.cartflow.coupon.dto.request.ApplyCouponRequest;
import com.cartflow.coupon.dto.request.CouponRequest;
import com.cartflow.coupon.dto.response.CouponDiscountResponse;
import com.cartflow.coupon.dto.response.CouponResponse;
import com.cartflow.coupon.entity.Coupon;
import com.cartflow.coupon.entity.CouponUsage;
import com.cartflow.coupon.entity.DiscountType;
import com.cartflow.coupon.repository.CouponRepository;
import com.cartflow.coupon.repository.CouponUsageRepository;
import com.cartflow.exception.DuplicateResourceException;
import com.cartflow.exception.InvalidCouponException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.user.entity.User;
import com.cartflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CartService cartService;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CouponResponse createCoupon(CouponRequest request) {

        if (couponRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException(
                    "A coupon with code '" + request.getCode() + "' already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minimumPurchase(request.getMinimumPurchase())
                .maximumDiscount(request.getMaximumDiscount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .expiryDate(request.getExpiryDate())
                .isActive(true)
                .build();

        Coupon saved = couponRepository.save(coupon);

        log.info("Coupon created: {}", saved.getCode());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CouponResponse updateCoupon(Long id, CouponRequest request) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        if (!coupon.getCode().equals(request.getCode())
                && couponRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException(
                    "A coupon with code '" + request.getCode() + "' already exists");
        }

        coupon.setCode(request.getCode());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinimumPurchase(request.getMinimumPurchase());
        coupon.setMaximumDiscount(request.getMaximumDiscount());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setExpiryDate(request.getExpiryDate());

        Coupon updated = couponRepository.save(coupon);

        log.info("Coupon updated: {}", updated.getCode());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCoupon(Long id) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        coupon.setActive(false);
        couponRepository.save(coupon);

        log.info("Coupon deactivated: {}", coupon.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CouponResponse> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable)
                .map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    @Override
    public BigDecimal validateAndCalculateDiscount(String code, BigDecimal cartTotal, User user) {

        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new InvalidCouponException("Invalid or inactive coupon code"));

        if (coupon.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidCouponException("This coupon has expired");
        }

        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new InvalidCouponException("This coupon has reached its usage limit");
        }

        if (couponUsageRepository.existsByCouponAndUser(coupon, user)) {
            throw new InvalidCouponException("You have already used this coupon");
        }

        if (cartTotal.compareTo(coupon.getMinimumPurchase()) < 0) {
            throw new InvalidCouponException(
                    "Minimum purchase of " + coupon.getMinimumPurchase() + " required for this coupon");
        }

        return calculateDiscount(coupon, cartTotal);
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal cartTotal) {

        BigDecimal discount;

        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = cartTotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (coupon.getMaximumDiscount() != null
                    && discount.compareTo(coupon.getMaximumDiscount()) > 0) {
                discount = coupon.getMaximumDiscount();
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        if (discount.compareTo(cartTotal) > 0) {
            discount = cartTotal;
        }

        return discount;
    }


    @Override
    @Transactional(readOnly = true)
    public CouponDiscountResponse validateCoupon(String email, String code) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal cartTotal = cartService.getMyCart(email).getTotalPrice();

        BigDecimal discount = validateAndCalculateDiscount(code, cartTotal, user);

        return buildDiscountResponse(code, cartTotal, discount);
    }

    @Override
    @Transactional
    public CouponDiscountResponse applyCoupon(String email, ApplyCouponRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal cartTotal = cartService.getMyCart(email).getTotalPrice();

        BigDecimal discount = validateAndCalculateDiscount(request.getCode(), cartTotal, user);

        recordCouponUsage(request.getCode(), user);

        log.info("Coupon {} applied for user: {}", request.getCode(), email);

        return buildDiscountResponse(request.getCode(), cartTotal, discount);
    }


    @Override
    @Transactional
    public void recordCouponUsage(String code, User user) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new InvalidCouponException("Invalid or inactive coupon code"));

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .user(user)
                .build();
        couponUsageRepository.save(usage);
    }


    private CouponDiscountResponse buildDiscountResponse(
            String code, BigDecimal cartTotal, BigDecimal discount) {

        return CouponDiscountResponse.builder()
                .code(code)
                .cartTotal(cartTotal)
                .discountAmount(discount)
                .finalAmount(cartTotal.subtract(discount))
                .build();
    }

    private CouponResponse mapToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minimumPurchase(coupon.getMinimumPurchase())
                .maximumDiscount(coupon.getMaximumDiscount())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .expiryDate(coupon.getExpiryDate())
                .isActive(coupon.isActive())
                .build();
    }
}