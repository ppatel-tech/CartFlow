package com.cartflow.admin.controller;

import com.cartflow.admin.dto.response.DashboardSummaryResponse;
import com.cartflow.admin.service.AdminDashboardService;
import com.cartflow.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping
    public ApiResponse<DashboardSummaryResponse> getDashboard() {
        DashboardSummaryResponse response = dashboardService.getDashboardSummary();
        return ApiResponse.success("Dashboard summary retrieved", response);
    }
}