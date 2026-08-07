package com.cartflow.inventory.service;

import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.inventory.dto.request.InventoryUpdateRequest;
import com.cartflow.inventory.dto.request.RestockRequest;
import com.cartflow.inventory.dto.response.InventoryResponse;
import com.cartflow.inventory.entity.Inventory;
import com.cartflow.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public InventoryResponse updateInventory(Long productId, InventoryUpdateRequest request) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for this product"));

        inventory.setAvailableQuantity(request.getAvailableQuantity());
        inventory.setLowStockThreshold(request.getLowStockThreshold());

        Inventory updated = inventoryRepository.save(inventory);

        log.info("Inventory updated for product {}: available={}", productId, updated.getAvailableQuantity());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public InventoryResponse restock(Long productId, RestockRequest request) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for this product"));

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.getQuantity());

        Inventory updated = inventoryRepository.save(inventory);

        log.info("Product {} restocked by {}. New available: {}",
                productId, request.getQuantity(), updated.getAvailableQuantity());

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public InventoryResponse getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for this product"));

        return mapToResponse(inventory);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        boolean isLowStock = inventory.getAvailableQuantity() <= inventory.getLowStockThreshold();

        return InventoryResponse.builder()
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .lowStock(isLowStock)
                .build();
    }
}