package com.cartflow.review.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.review.dto.request.ReviewRequest;
import com.cartflow.review.dto.response.ReviewResponse;
import com.cartflow.review.service.ReviewService;
import com.cartflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {

        ReviewResponse response = reviewService.addReview(principal.getEmail(), productId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review added successfully", response));
    }

    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {

        ReviewResponse response = reviewService.updateReview(principal.getEmail(), reviewId, request);
        return ApiResponse.success("Review updated successfully", response);
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId,
            @PathVariable Long reviewId) {

        reviewService.deleteReview(principal.getEmail(), reviewId);
        return ApiResponse.success("Review deleted successfully", null);
    }

    @GetMapping
    public ApiResponse<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {

        Page<ReviewResponse> response = reviewService.getProductReviews(productId, pageable);
        return ApiResponse.success("Reviews retrieved successfully", response);
    }
}