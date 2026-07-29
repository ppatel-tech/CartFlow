package com.cartflow.address.dto.response;

import com.cartflow.address.entity.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private AddressType addressType;
    private boolean isDefault;
}