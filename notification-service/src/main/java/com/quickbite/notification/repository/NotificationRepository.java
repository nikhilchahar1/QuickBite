package com.quickbite.notification.repository;

import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // All notifications for a user — newest first
    List<Notification> findByRecipientIdOrderBySentAtDesc(Long recipientId);

    // Only unread notifications for a user
    List<Notification> findByRecipientIdAndIsReadFalse(Long recipientId);

    // Count unread — used for the notification badge
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    // Notifications by type — for analytics
    List<Notification> findByType(NotificationType type);

    // Notifications related to a specific order
    List<Notification> findByRelatedId(Long relatedId);

    // marks all as read in one query
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, " +
            "n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.recipientId = :recipientId " +
            "AND n.isRead = false")
    int markAllAsRead(@Param("recipientId") Long recipientId);
}