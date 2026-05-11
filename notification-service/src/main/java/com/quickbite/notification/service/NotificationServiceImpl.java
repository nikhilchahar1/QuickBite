package com.quickbite.notification.service;

import com.quickbite.notification.dto.BulkNotificationRequest;
import com.quickbite.notification.dto.NotificationResponse;
import com.quickbite.notification.dto.SendNotificationRequest;
import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.enums.NotificationChannel;
import com.quickbite.notification.enums.NotificationType;
import com.quickbite.notification.exception.BadRequestException;
import com.quickbite.notification.exception.ResourceNotFoundException;
import com.quickbite.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationResponse send(SendNotificationRequest request) {

        Notification notification = new Notification();
        notification.setRecipientId(request.getRecipientId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setChannel(
                request.getChannel() != null ? request.getChannel() : NotificationChannel.IN_APP);
        notification.setRelatedId(request.getRelatedId());
        notification.setRelatedType(request.getRelatedType());
        // @PrePersist sets sentAt and isRead=false

        Notification saved = notificationRepository.save(notification);

        // Log that notification was sent
        log.info("Notification sent to user {} : {}", request.getRecipientId(), request.getTitle());

        return NotificationResponse.from(saved);
    }

    // Admin sends same notification to multiple users
    @Override
    @Transactional
    public List<NotificationResponse> sendBulk(BulkNotificationRequest request) {

        List<NotificationResponse> responses = new ArrayList<>();

        // Create one notification per recipient
        for (Long recipientId : request.getRecipientIds()) {
            Notification notification = new Notification();
            notification.setRecipientId(recipientId);
            notification.setType(request.getType());
            notification.setTitle(request.getTitle());
            notification.setMessage(request.getMessage());
            notification.setChannel(NotificationChannel.IN_APP);

            Notification saved = notificationRepository.save(notification);
            responses.add(NotificationResponse.from(saved));
        }

        log.info("Bulk notification sent to {} users", request.getRecipientIds().size());

        return responses;
    }

    @Override
    public List<NotificationResponse> getByRecipient(Long recipientId) {
        return notificationRepository
                .findByRecipientIdOrderBySentAtDesc(recipientId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }


    @Override
    public List<NotificationResponse> getUnreadByRecipient(Long recipientId) {
        return notificationRepository
                .findByRecipientIdAndIsReadFalse(recipientId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    // Returns the number shown on the notification bell badge
    @Override
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long recipientId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        // Security check — users can only mark their OWN
        // notifications as read
        if (!notification.getRecipientId().equals(recipientId)) {
            throw new BadRequestException("You can only mark your own notifications as read");
        }

        // Only update if not already read
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return NotificationResponse.from(notification);
    }

    // Uses @Modifying @Query — one SQL UPDATE instead of N saves
    @Override
    @Transactional
    public int markAllAsRead(Long recipientId) {
        int updatedCount = notificationRepository.markAllAsRead(recipientId);
        log.info("Marked {} notifications as read for user {}", updatedCount, recipientId);
        return updatedCount;
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long recipientId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        // Users can only delete their own notifications
        if (!notification.getRecipientId().equals(recipientId)) {
            throw new BadRequestException("You can only delete your own notifications");
        }

        notificationRepository.deleteById(notificationId);
        log.info("Notification {} deleted by user {}", notificationId, recipientId);
    }

    @Override
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll()
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse sendOrderNotification(
            Long recipientId, Long orderId,
            String type, String title, String message) {

        SendNotificationRequest request = new SendNotificationRequest();
        request.setRecipientId(recipientId);
        request.setType(NotificationType.valueOf(type));
        request.setTitle(title);
        request.setMessage(message);
        request.setChannel(NotificationChannel.IN_APP);
        request.setRelatedId(orderId);
        request.setRelatedType("ORDER");

        return send(request);
    }
}