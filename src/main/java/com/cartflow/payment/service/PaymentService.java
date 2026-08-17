package com.cartflow.payment.service;

import com.cartflow.payment.dto.request.InitiatePaymentRequest;
import com.cartflow.payment.dto.request.VerifyPaymentRequest;
import com.cartflow.payment.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse initiatePayment(String email, InitiatePaymentRequest request);
    PaymentResponse verifyPayment(String email, VerifyPaymentRequest request);
}