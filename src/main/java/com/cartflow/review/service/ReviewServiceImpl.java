package com.cartflow.review.service;

import com.cartflow.exception.BusinessException;
import com.cartflow.exception.DuplicateResourceException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.order.entity.Order;
import com.cartflow.order.entity.OrderStatus;
import com.cartflow.order.repository.OrderItemRepository;
import com.cartflow.order.repository.OrderRepository;
import com.cartflow.product.entity.Product;
import com.cartflow.product.repository.ProductRepository;
import com.cartflow.review.dto.request.ReviewRequest;
import com.cartflow.review.dto.response.ReviewResponse;
import com.cartflow.review.entity.Review;
import com.cartflow.review.repository.ReviewRepository;
import com.cartflow.user.entity.User;
import com.cartflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public ReviewResponse addReview(String email, Long productId, ReviewRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (reviewRepository.existsByProductAndUser(product, user)) {
            throw new DuplicateResourceException("You have already reviewed this product");
        }

        boolean isEligible = orderItemRepository.existsDeliveredPurchase(productId, user.getId());
        if (!isEligible) {
            throw new BusinessException(
                    "You can only review products from delivered orders");
        }

        Order qualifyingOrder = findQualifyingOrder(product, user);

        Review review = Review.builder()
                .product(product)
                .user(user)
                .order(qualifyingOrder)
                .rating(request.getRating())
                .review(request.getReview())
                .build();

        Review savedReview = reviewRepository.save(review);

        recalculateProductRating(product);

        log.info("Review added for product {} by user: {}", product.getSku(), email);

        return mapToResponse(savedReview);
    }


    @Override
    @Transactional
    public ReviewResponse updateReview(String email, Long reviewId, ReviewRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = reviewRepository.findByIdAndUser(reviewId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setRating(request.getRating());
        review.setReview(request.getReview());

        Review updatedReview = reviewRepository.save(review);

        recalculateProductRating(review.getProduct());

        log.info("Review {} updated by user: {}", reviewId, email);

        return mapToResponse(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(String email, Long reviewId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = reviewRepository.findByIdAndUser(reviewId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        Product product = review.getProduct();

        reviewRepository.delete(review);

        recalculateProductRating(product);

        log.info("Review {} deleted by user: {}", reviewId, email);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {

        Product product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return reviewRepository.findByProduct(product, pageable)
                .map(this::mapToResponse);
    }

    private Order findQualifyingOrder(Product product, User user) {
        return orderRepository.findByCustomer(user, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .filter(order -> orderItemRepository.findByOrder(order).stream()
                        .anyMatch(item -> item.getProduct().getId().equals(product.getId())))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No qualifying delivered order found"));
    }

    private void recalculateProductRating(Product product) {
        List<Review> allReviews = reviewRepository.findByProduct(product);

        int totalReviews = allReviews.size();
        BigDecimal averageRating = allReviews.isEmpty()
                ? BigDecimal.ZERO
                : allReviews.stream()
                  .map(r -> BigDecimal.valueOf(r.getRating()))
                  .reduce(BigDecimal.ZERO, BigDecimal::add)
                  .divide(BigDecimal.valueOf(totalReviews), 2, RoundingMode.HALF_UP);

        product.setAverageRating(averageRating);
        product.setTotalReviews(totalReviews);
        productRepository.save(product);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .productId(review.getProduct().getId())
                .reviewerName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .review(review.getReview())
                .createdAt(review.getCreatedAt())
                .build();
    }
}