package com.cartflow.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private String reviewerName;
    private Integer rating;
    private String review;
    private Instant createdAt;
}