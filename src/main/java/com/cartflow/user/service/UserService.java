package com.cartflow.user.service;

import com.cartflow.user.dto.request.UpdateProfileRequest;
import com.cartflow.user.dto.response.UserResponse;

public interface UserService {
    UserResponse getCurrentUserProfile(String email);
    UserResponse updateProfile(String email, UpdateProfileRequest request);
    void deleteCurrentUser(String email);
}