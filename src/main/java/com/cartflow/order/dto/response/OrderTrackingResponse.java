package com.cartflow.order.dto.response;

import com.cartflow.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class OrderTrackingResponse {
    private String orderNumber;
    private OrderStatus currentStatus;
    private Instant lastUpdated;
    private String statusDescription;
}