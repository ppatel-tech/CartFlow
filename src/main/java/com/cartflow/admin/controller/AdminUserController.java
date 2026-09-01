package com.cartflow.admin.controller;

import com.cartflow.admin.dto.request.UpdateUserStatusRequest;
import com.cartflow.common.ApiResponse;
import com.cartflow.user.dto.response.UserResponse;
import com.cartflow.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<Page<UserResponse>> getAllUsers(@ParameterObject Pageable pageable) {
        Page<UserResponse> response = userService.getAllUsersForAdmin(pageable);
        return ApiResponse.success("Users retrieved successfully", response);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UserResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        UserResponse response = userService.updateUserStatus(id, request.getStatus());
        return ApiResponse.success("User status updated successfully", response);
    }
}