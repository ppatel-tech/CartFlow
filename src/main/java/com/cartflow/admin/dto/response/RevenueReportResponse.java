package com.cartflow.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class RevenueReportResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalTaxCollected;
    private BigDecimal totalDiscountsGiven;
    private long totalSuccessfulPayments;
}