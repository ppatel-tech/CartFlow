package com.cartflow.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class OrderConfigResponse {
    private BigDecimal taxRate;
    private BigDecimal shippingCharge;
}