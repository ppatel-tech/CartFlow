package com.cartflow.notification.repository;

import com.cartflow.notification.entity.Notification;
import com.cartflow.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Optional<Notification> findByIdAndUser(Long id, User user);
}