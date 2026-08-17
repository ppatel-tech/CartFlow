package com.cartflow.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPaymentRequest {

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    @NotNull(message = "Payment outcome is required")
    private Boolean success;
}