package com.cartflow.inventory.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.inventory.dto.request.InventoryUpdateRequest;
import com.cartflow.inventory.dto.request.RestockRequest;
import com.cartflow.inventory.dto.response.InventoryResponse;
import com.cartflow.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ApiResponse<InventoryResponse> getInventory(@PathVariable Long productId) {
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);
        return ApiResponse.success("Inventory retrieved successfully", response);
    }

    @PutMapping("/{productId}")
    public ApiResponse<InventoryResponse> updateInventory(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryUpdateRequest request) {

        InventoryResponse response = inventoryService.updateInventory(productId, request);
        return ApiResponse.success("Inventory updated successfully", response);
    }

    @PatchMapping("/{productId}/restock")
    public ApiResponse<InventoryResponse> restock(
            @PathVariable Long productId,
            @Valid @RequestBody RestockRequest request) {

        InventoryResponse response = inventoryService.restock(productId, request);
        return ApiResponse.success("Product restocked successfully", response);
    }
}