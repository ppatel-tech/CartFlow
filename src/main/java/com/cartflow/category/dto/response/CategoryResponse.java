package com.cartflow.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class CategoryResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private boolean isActive;
}