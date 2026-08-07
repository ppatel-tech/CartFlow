package com.cartflow.inventory.service;

import com.cartflow.inventory.dto.request.InventoryUpdateRequest;
import com.cartflow.inventory.dto.request.RestockRequest;
import com.cartflow.inventory.dto.response.InventoryResponse;

public interface InventoryService {
    InventoryResponse updateInventory(Long productId, InventoryUpdateRequest request);
    InventoryResponse restock(Long productId, RestockRequest request);
    InventoryResponse getInventoryByProductId(Long productId);
}