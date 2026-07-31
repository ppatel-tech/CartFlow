package com.cartflow.brand.service;

import com.cartflow.brand.dto.request.BrandRequest;
import com.cartflow.brand.dto.response.BrandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {
    BrandResponse createBrand(BrandRequest request);
    BrandResponse updateBrand(Long id, BrandRequest request);
    void deleteBrand(Long id);
    BrandResponse getBrandById(Long id);
    Page<BrandResponse> getAllBrands(Pageable pageable);
    Page<BrandResponse> getAllBrandsForAdmin(Pageable pageable);
}