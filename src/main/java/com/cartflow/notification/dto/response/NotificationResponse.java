package com.cartflow.notification.dto.response;

import com.cartflow.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType notificationType;
    private boolean isRead;
    private Instant createdAt;
}