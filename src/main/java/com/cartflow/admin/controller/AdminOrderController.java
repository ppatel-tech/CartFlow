package com.cartflow.admin.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.order.dto.request.UpdateOrderStatusRequest;
import com.cartflow.order.dto.response.OrderResponse;
import com.cartflow.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<Page<OrderResponse>> getAllOrders(@ParameterObject Pageable pageable) {
        Page<OrderResponse> response = orderService.getAllOrdersForAdmin(pageable);
        return ApiResponse.success("Orders retrieved successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderByIdForAdmin(id);
        return ApiResponse.success("Order retrieved successfully", response);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());
        return ApiResponse.success("Order status updated successfully", response);
    }
}