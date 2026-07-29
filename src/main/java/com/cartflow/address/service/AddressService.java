package com.cartflow.address.service;

import com.cartflow.address.dto.request.AddressRequest;
import com.cartflow.address.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressResponse createAddress(String email, AddressRequest request);
    List<AddressResponse> getMyAddresses(String email);
    AddressResponse updateAddress(String email, Long addressId, AddressRequest request);
    void deleteAddress(String email, Long addressId);
    AddressResponse setDefaultAddress(String email, Long addressId);
}