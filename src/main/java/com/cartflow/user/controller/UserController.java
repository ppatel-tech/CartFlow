package com.cartflow.user.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.security.UserPrincipal;
import com.cartflow.user.dto.request.UpdateProfileRequest;
import com.cartflow.user.dto.response.UserResponse;
import com.cartflow.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {

        UserResponse response = userService.getCurrentUserProfile(principal.getEmail());

        return ApiResponse.success("Profile retrieved successfully", response);
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserResponse response = userService.updateProfile(principal.getEmail(), request);

        return ApiResponse.success("Profile updated successfully", response);
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {

        userService.deleteCurrentUser(principal.getEmail());

        return ApiResponse.success("Account deleted successfully", null);
    }
}