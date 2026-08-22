package com.cartflow.notification.service;

import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.notification.dto.response.NotificationResponse;
import com.cartflow.notification.entity.Notification;
import com.cartflow.notification.entity.NotificationType;
import com.cartflow.notification.repository.NotificationRepository;
import com.cartflow.user.entity.User;
import com.cartflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;


    @Transactional
    public void notifyUser(User user, String title, String message, boolean sendEmail) {

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .notificationType(NotificationType.EMAIL)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        if (sendEmail) {
            emailService.sendEmail(user.getEmail(), title, message);
        }
    }


    public Page<NotificationResponse> getMyNotifications(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public NotificationResponse markAsRead(String email, Long notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = notificationRepository.findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);

        return mapToResponse(updated);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationType(notification.getNotificationType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}