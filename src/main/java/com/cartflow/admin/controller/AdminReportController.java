package com.cartflow.admin.controller;

import com.cartflow.admin.dto.response.*;
import com.cartflow.admin.service.AdminReportService;
import com.cartflow.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService reportService;

    @GetMapping("/revenue")
    public ApiResponse<RevenueReportResponse> getRevenueReport() {
        return ApiResponse.success("Revenue report retrieved", reportService.getRevenueReport());
    }

    @GetMapping("/products")
    public ApiResponse<List<ProductReportResponse>> getProductReport() {
        return ApiResponse.success("Product report retrieved", reportService.getProductReport());
    }

    @GetMapping("/customers")
    public ApiResponse<List<CustomerReportResponse>> getCustomerReport() {
        return ApiResponse.success("Customer report retrieved", reportService.getCustomerReport());
    }

    @GetMapping("/sales-summary")
    public ApiResponse<SalesSummaryResponse> getSalesSummary() {
        return ApiResponse.success("Sales summary retrieved", reportService.getSalesSummary());
    }
}