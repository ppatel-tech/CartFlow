package com.cartflow.notification.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.notification.dto.response.NotificationResponse;
import com.cartflow.notification.service.NotificationService;
import com.cartflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @ParameterObject Pageable pageable) {

        Page<NotificationResponse> response =
                notificationService.getMyNotifications(principal.getEmail(), pageable);
        return ApiResponse.success("Notifications retrieved successfully", response);
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        NotificationResponse response = notificationService.markAsRead(principal.getEmail(), id);
        return ApiResponse.success("Notification marked as read", response);
    }
}