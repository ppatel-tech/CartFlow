package com.cartflow.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class CustomerReportResponse {
    private Long customerId;
    private String customerName;
    private long totalOrders;
    private BigDecimal totalSpent;
}