package com.cartflow.address.controller;

import com.cartflow.address.dto.request.AddressRequest;
import com.cartflow.address.dto.response.AddressResponse;
import com.cartflow.address.service.AddressService;
import com.cartflow.common.ApiResponse;
import com.cartflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddressRequest request) {

        AddressResponse response = addressService.createAddress(principal.getEmail(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully", response));
    }

    @GetMapping
    public ApiResponse<List<AddressResponse>> getMyAddresses(
            @AuthenticationPrincipal UserPrincipal principal) {

        List<AddressResponse> addresses = addressService.getMyAddresses(principal.getEmail());

        return ApiResponse.success("Addresses retrieved successfully", addresses);
    }

    @PutMapping("/{addressId}")
    public ApiResponse<AddressResponse> updateAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {

        AddressResponse response = addressService.updateAddress(
                principal.getEmail(), addressId, request);

        return ApiResponse.success("Address updated successfully", response);
    }

    @DeleteMapping("/{addressId}")
    public ApiResponse<Void> deleteAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long addressId) {

        addressService.deleteAddress(principal.getEmail(), addressId);

        return ApiResponse.success("Address deleted successfully", null);
    }


    @PatchMapping("/{addressId}/default")
    public ApiResponse<AddressResponse> setDefaultAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long addressId) {

        AddressResponse response = addressService.setDefaultAddress(
                principal.getEmail(), addressId);

        return ApiResponse.success("Default address updated successfully", response);
    }
}