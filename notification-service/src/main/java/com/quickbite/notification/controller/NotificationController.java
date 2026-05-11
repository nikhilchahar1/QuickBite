package com.quickbite.notification.controller;

import com.quickbite.notification.dto.BulkNotificationRequest;
import com.quickbite.notification.dto.NotificationResponse;
import com.quickbite.notification.dto.SendNotificationRequest;
import com.quickbite.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private Long getCustomerId(@RequestHeader("X-User-Id") String userId) {
        return Long.parseLong(userId);
    }

    // Admin or internal service sends a notification
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.send(request));
    }

    // Admin broadcasts to multiple users
    @PostMapping("/send-bulk")
    public ResponseEntity<List<NotificationResponse>> sendBulk(
            @Valid @RequestBody BulkNotificationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED).body(notificationService.sendBulk(request));
    }

    // Logged-in user gets all their notifications
    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestHeader("X-User-Id") String userId) {
        Long recipientId = Long.parseLong(userId);
        return ResponseEntity.ok(notificationService.getByRecipient(recipientId));
    }

    // Only unread notifications
    @GetMapping("/my/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(
            @RequestHeader("X-User-Id") String userId) {
        Long recipientId = Long.parseLong(userId);
        return ResponseEntity.ok(notificationService.getUnreadByRecipient(recipientId));
    }

    // The number shown on the notification bell badge
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("X-User-Id") String userId) {
        Long recipientId = Long.parseLong(userId);
        long count = notificationService.getUnreadCount(recipientId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Mark one notification as read
    @PutMapping("/read/{id}")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id, @RequestHeader("X-User-Id") String userId) {
        Long recipientId = Long.parseLong(userId);
        return ResponseEntity.ok(notificationService.markAsRead(id, recipientId));
    }

    // Mark ALL notifications as read
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead(
            @RequestHeader("X-User-Id") String userId) {
        Long recipientId = Long.parseLong(userId);
        int count = notificationService.markAllAsRead(recipientId);
        return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read", "updatedCount", count
        ));
    }

    // Delete a notification
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id, @RequestHeader("X-User-Id") String userId) {
        Long recipientId = Long.parseLong(userId);
        notificationService.deleteNotification(id, recipientId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    // Admin sees all notifications
    @GetMapping("/all")
    public ResponseEntity<List<NotificationResponse>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }
}