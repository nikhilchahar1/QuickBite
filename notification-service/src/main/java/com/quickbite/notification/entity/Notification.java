package com.quickbite.notification.entity;

import com.quickbite.notification.enums.NotificationChannel;
import com.quickbite.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications",
        indexes = {
                // get all notifications for user
                @Index(name = "idx_recipient", columnList = "recipientId"),
                // for fast unread count query
                @Index(name = "idx_is_read", columnList = "isRead")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // Short heading shown in notification
    @Column(nullable = false)
    private String title;

    // Full message shown when notification is opened
    @Column(nullable = false, length = 500)
    private String message;

    // How it was sent
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column
    private Long relatedId;

    // What relatedId refers to
    // e.g. "ORDER", "PROMO", "RESTAURANT"
    @Column
    private String relatedType;

    @Column(nullable = false)
    private boolean isRead = false;

    // When was this notification sent
    @Column(nullable = false)
    private LocalDateTime sentAt;

    // When was it read (null = not yet read)
    @Column
    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }
}