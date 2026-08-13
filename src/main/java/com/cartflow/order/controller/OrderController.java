package com.cartflow.order.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.order.dto.request.CheckoutRequest;
import com.cartflow.order.dto.response.OrderConfigResponse;
import com.cartflow.order.dto.response.OrderResponse;
import com.cartflow.order.dto.response.OrderTrackingResponse;
import com.cartflow.order.service.OrderService;
import com.cartflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CheckoutRequest request) {

        OrderResponse response = orderService.checkout(principal.getEmail(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", response));
    }


    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        OrderResponse response = orderService.getOrderById(principal.getEmail(), id);
        return ApiResponse.success("Order retrieved successfully", response);
    }

    @GetMapping
    public ApiResponse<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @ParameterObject Pageable pageable) {

        Page<OrderResponse> response = orderService.getMyOrders(principal.getEmail(), pageable);
        return ApiResponse.success("Orders retrieved successfully", response);
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        OrderResponse response = orderService.cancelOrder(principal.getEmail(), id);
        return ApiResponse.success("Order cancelled successfully", response);
    }

    @GetMapping("/{id}/tracking")
    public ApiResponse<OrderTrackingResponse> trackOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        OrderTrackingResponse response = orderService.trackOrder(principal.getEmail(), id);
        return ApiResponse.success("Tracking information retrieved", response);
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> getInvoice(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        byte[] pdfBytes = orderService.getInvoicePdf(principal.getEmail(), id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice.pdf")
                .body(pdfBytes);
    }

    @GetMapping("/config")
    public ApiResponse<OrderConfigResponse> getOrderConfig() {
        OrderConfigResponse response = orderService.getOrderConfig();
        return ApiResponse.success("Order configuration retrieved", response);
    }
}