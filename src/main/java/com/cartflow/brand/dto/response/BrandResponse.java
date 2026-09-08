package com.cartflow.brand.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class BrandResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private boolean isActive;
}