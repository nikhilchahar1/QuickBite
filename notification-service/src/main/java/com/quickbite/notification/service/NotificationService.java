package com.quickbite.notification.service;

import com.quickbite.notification.dto.BulkNotificationRequest;
import com.quickbite.notification.dto.NotificationResponse;
import com.quickbite.notification.dto.SendNotificationRequest;

import java.util.List;

public interface NotificationService {

    // Send single notification
    NotificationResponse send(SendNotificationRequest request);

    // Send to multiple recipients (admin broadcast)
    List<NotificationResponse> sendBulk(BulkNotificationRequest request);

    // Get all notifications for a user
    List<NotificationResponse> getByRecipient(Long recipientId);

    // Get only unread notifications for a user
    List<NotificationResponse> getUnreadByRecipient(Long recipientId);

    // Count unread — for the notification bell badge
    long getUnreadCount(Long recipientId);

    // Mark one notification as read
    NotificationResponse markAsRead(Long notificationId, Long recipientId);

    // Mark ALL notifications as read for a user
    int markAllAsRead(Long recipientId);

    // Delete a notification
    void deleteNotification(Long notificationId, Long recipientId);

    // Admin: get all notifications on the platform
    List<NotificationResponse> getAll();

    // Helper: create order lifecycle notifications
    NotificationResponse sendOrderNotification(
            Long recipientId, Long orderId,
            String type, String title, String message);
}