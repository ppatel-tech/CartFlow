package com.cartflow.address.service;

import com.cartflow.address.dto.request.AddressRequest;
import com.cartflow.address.dto.response.AddressResponse;
import com.cartflow.address.entity.Address;
import com.cartflow.address.repository.AddressRepository;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.user.entity.User;
import com.cartflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AddressResponse createAddress(String email, AddressRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isFirstAddress = addressRepository.findByUser(user).isEmpty();

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .addressType(request.getAddressType())
                .isDefault(isFirstAddress)
                .build();

        Address savedAddress = addressRepository.save(address);

        log.info("Address created for user: {}", email);

        return mapToResponse(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return addressRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String email, Long addressId, AddressRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setAddressType(request.getAddressType());

        Address updatedAddress = addressRepository.save(address);

        log.info("Address {} updated for user: {}", addressId, email);

        return mapToResponse(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(String email, Long addressId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        boolean wasDefault = address.isDefault();

        addressRepository.delete(address);

        if (wasDefault) {
            addressRepository.findByUser(user).stream()
                    .findFirst()
                    .ifPresent(nextAddress -> {
                        nextAddress.setDefault(true);
                        addressRepository.save(nextAddress);
                    });
        }

        log.info("Address {} deleted for user: {}", addressId, email);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(String email, Long addressId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address newDefault = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(currentDefault -> {
                    if (!currentDefault.getId().equals(newDefault.getId())) {
                        currentDefault.setDefault(false);
                        addressRepository.save(currentDefault);
                    }
                });

        newDefault.setDefault(true);
        Address savedAddress = addressRepository.save(newDefault);

        log.info("Default address changed to {} for user: {}", addressId, email);

        return mapToResponse(savedAddress);
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .addressType(address.getAddressType())
                .isDefault(address.isDefault())
                .build();
    }
}