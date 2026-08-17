package com.cartflow.payment.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.payment.dto.request.InitiatePaymentRequest;
import com.cartflow.payment.dto.request.VerifyPaymentRequest;
import com.cartflow.payment.dto.response.PaymentResponse;
import com.cartflow.payment.service.PaymentService;
import com.cartflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ApiResponse<PaymentResponse> initiatePayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InitiatePaymentRequest request) {

        PaymentResponse response = paymentService.initiatePayment(principal.getEmail(), request);
        return ApiResponse.success("Payment initiated", response);
    }


    @PostMapping("/verify")
    public ApiResponse<PaymentResponse> verifyPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VerifyPaymentRequest request) {

        PaymentResponse response = paymentService.verifyPayment(principal.getEmail(), request);
        return ApiResponse.success("Payment verification processed", response);
    }
}

