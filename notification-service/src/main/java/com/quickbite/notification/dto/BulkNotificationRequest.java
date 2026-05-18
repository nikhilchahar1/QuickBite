package com.quickbite.notification.dto;

import com.quickbite.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

// Admin broadcasts to multiple users at once
@Data
public class BulkNotificationRequest {

    // List of userIds to notify
    @NotEmpty(message = "At least one recipient is required")
    private List<Long> recipientIds;

    @NotNull(message = "Type is required")
    private NotificationType type;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;
}