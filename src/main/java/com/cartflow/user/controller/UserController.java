package com.cartflow.user.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ApiResponse.success("Current user retrieved", Map.of(
                "id", principal.getId(),
                "email", principal.getEmail(),
                "role", principal.getRole()
        ));
    }
}