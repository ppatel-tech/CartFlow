package com.cartflow.brand.service;

import com.cartflow.brand.dto.request.BrandRequest;
import com.cartflow.brand.dto.response.BrandResponse;
import com.cartflow.brand.entity.Brand;
import com.cartflow.brand.repository.BrandRepository;
import com.cartflow.exception.DuplicateResourceException;
import com.cartflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponse createBrand(BrandRequest request) {

        if (brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "A brand with name '" + request.getName() + "' already exists");
        }

        Brand brand = Brand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();

        Brand savedBrand = brandRepository.save(brand);

        log.info("Brand created: {}", savedBrand.getName());

        return mapToResponse(savedBrand);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponse updateBrand(Long id, BrandRequest request) {

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        if (!brand.getName().equals(request.getName())
                && brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "A brand with name '" + request.getName() + "' already exists");
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());

        Brand updatedBrand = brandRepository.save(brand);

        log.info("Brand updated: {}", updatedBrand.getName());

        return mapToResponse(updatedBrand);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBrand(Long id) {

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        brand.setActive(false);
        brandRepository.save(brand);

        log.info("Brand soft-deleted: {}", brand.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        return mapToResponse(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> getAllBrands(Pageable pageable) {
        return brandRepository.findByIsActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<BrandResponse> getAllBrandsForAdmin(Pageable pageable) {
        return brandRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private BrandResponse mapToResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .isActive(brand.isActive())
                .build();
    }
}