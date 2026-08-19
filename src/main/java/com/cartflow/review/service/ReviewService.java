package com.cartflow.review.service;

import com.cartflow.review.dto.request.ReviewRequest;
import com.cartflow.review.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse addReview(String email, Long productId, ReviewRequest request);
    ReviewResponse updateReview(String email, Long reviewId, ReviewRequest request);
    void deleteReview(String email, Long reviewId);
    Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable);
}