package com.quickbite.notification.dto;

import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.enums.NotificationChannel;
import com.quickbite.notification.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {

    private Long notificationId;
    private Long recipientId;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationChannel channel;
    private Long relatedId;
    private String relatedType;
    private boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    // Static factory method
    public static NotificationResponse from(Notification n) {
        NotificationResponse res = new NotificationResponse();
        res.setNotificationId(n.getNotificationId());
        res.setRecipientId(n.getRecipientId());
        res.setType(n.getType());
        res.setTitle(n.getTitle());
        res.setMessage(n.getMessage());
        res.setChannel(n.getChannel());
        res.setRelatedId(n.getRelatedId());
        res.setRelatedType(n.getRelatedType());
        res.setRead(n.isRead());
        res.setSentAt(n.getSentAt());
        res.setReadAt(n.getReadAt());
        return res;
    }
}