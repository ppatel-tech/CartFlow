package com.cartflow.user.service;

import com.cartflow.user.dto.request.UpdateProfileRequest;
import com.cartflow.user.dto.response.UserResponse;
import com.cartflow.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse getCurrentUserProfile(String email);
    UserResponse updateProfile(String email, UpdateProfileRequest request);
    void deleteCurrentUser(String email);
    Page<UserResponse> getAllUsersForAdmin(Pageable pageable);
    UserResponse updateUserStatus(Long userId, UserStatus status);
}