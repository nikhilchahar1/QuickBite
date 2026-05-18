package com.quickbite.notification.dto;

import com.quickbite.notification.enums.NotificationChannel;
import com.quickbite.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Used to send a single notification
// Called by other services or admin
@Data
public class SendNotificationRequest {

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    // Default to IN_APP if not specified
    private NotificationChannel channel = NotificationChannel.IN_APP;

    // Optional — which order/promo this relates to
    private Long relatedId;
    private String relatedType;
}