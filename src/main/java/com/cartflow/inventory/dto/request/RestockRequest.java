package com.cartflow.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestockRequest {

    @NotNull(message = "Restock quantity is required")
    @Min(value = 1, message = "Restock quantity must be positive")
    private Integer quantity;
}