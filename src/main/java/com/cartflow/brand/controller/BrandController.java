package com.cartflow.brand.controller;

import com.cartflow.brand.dto.request.BrandRequest;
import com.cartflow.brand.dto.response.BrandResponse;
import com.cartflow.brand.service.BrandService;
import com.cartflow.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @Valid @RequestBody BrandRequest request) {

        BrandResponse response = brandService.createBrand(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Brand created successfully", response));
    }

    @PutMapping("/{id}")
    public ApiResponse<BrandResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request) {

        BrandResponse response = brandService.updateBrand(id, request);

        return ApiResponse.success("Brand updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ApiResponse.success("Brand deleted successfully", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<BrandResponse> getBrandById(@PathVariable Long id) {
        BrandResponse response = brandService.getBrandById(id);
        return ApiResponse.success("Brand retrieved successfully", response);
    }

    @GetMapping
    public ApiResponse<Page<BrandResponse>> getAllBrands(@ParameterObject Pageable pageable) {
        Page<BrandResponse> response = brandService.getAllBrands(pageable);
        return ApiResponse.success("Brands retrieved successfully", response);
    }

    @GetMapping("/admin")
    public ApiResponse<Page<BrandResponse>> getAllBrandsForAdmin(
            @ParameterObject Pageable pageable) {

        Page<BrandResponse> response = brandService.getAllBrandsForAdmin(pageable);
        return ApiResponse.success("All brands retrieved successfully", response);
    }
}