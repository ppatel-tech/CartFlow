package com.cartflow.admin.service;

import com.cartflow.admin.dto.response.*;
import com.cartflow.order.entity.OrderStatus;
import com.cartflow.order.entity.PaymentStatus;
import com.cartflow.order.repository.OrderItemRepository;
import com.cartflow.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public RevenueReportResponse getRevenueReport() {
        return RevenueReportResponse.builder()
                .totalRevenue(orderRepository.getTotalRevenue())
                .totalTaxCollected(orderRepository.getTotalTaxCollected())
                .totalDiscountsGiven(orderRepository.getTotalDiscountsGiven())
                .totalSuccessfulPayments(orderRepository.countByPaymentStatus(PaymentStatus.SUCCESS))
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductReportResponse> getProductReport() {
        return orderItemRepository.getProductSalesReport().stream()
                .map(row -> ProductReportResponse.builder()
                        .productId((Long) row[0])
                        .productName((String) row[1])
                        .unitsSold(((Number) row[2]).longValue())
                        .revenueGenerated((BigDecimal) row[3])
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<CustomerReportResponse> getCustomerReport() {
        return orderRepository.getCustomerSalesReport().stream()
                .map(row -> CustomerReportResponse.builder()
                        .customerId((Long) row[0])
                        .customerName((String) row[1])
                        .totalOrders(((Number) row[2]).longValue())
                        .totalSpent((BigDecimal) row[3])
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public SalesSummaryResponse getSalesSummary() {

        long totalOrders = orderRepository.count();
        long deliveredOrders = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByOrderStatus(OrderStatus.CANCELLED);
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        long successfulPayments = orderRepository.countByPaymentStatus(PaymentStatus.SUCCESS);

        BigDecimal averageOrderValue = successfulPayments == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(successfulPayments), 2, RoundingMode.HALF_UP);

        return SalesSummaryResponse.builder()
                .totalOrders(totalOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .build();
    }
}