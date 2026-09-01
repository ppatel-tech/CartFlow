package com.cartflow.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ProductReportResponse {
    private Long productId;
    private String productName;
    private long unitsSold;
    private BigDecimal revenueGenerated;
}