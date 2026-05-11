package com.quickbite.notification;

import com.quickbite.notification.dto.BulkNotificationRequest;
import com.quickbite.notification.dto.NotificationResponse;
import com.quickbite.notification.dto.SendNotificationRequest;
import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.enums.NotificationChannel;
import com.quickbite.notification.enums.NotificationType;
import com.quickbite.notification.exception.BadRequestException;
import com.quickbite.notification.exception.ResourceNotFoundException;
import com.quickbite.notification.repository.NotificationRepository;
import com.quickbite.notification.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification unreadNotification;
    private Notification readNotification;
    private SendNotificationRequest sendRequest;
    private BulkNotificationRequest bulkRequest;

    @BeforeEach
    void setUp() {

        // An unread notification for user 1
        unreadNotification = new Notification();
        unreadNotification.setNotificationId(1L);
        unreadNotification.setRecipientId(1L);
        unreadNotification.setType(NotificationType.ORDER_PLACED);
        unreadNotification.setTitle("Order Placed!");
        unreadNotification.setMessage("Your order #10 has been placed successfully.");
        unreadNotification.setChannel(NotificationChannel.IN_APP);
        unreadNotification.setRelatedId(10L);
        unreadNotification.setRelatedType("ORDER");
        unreadNotification.setRead(false);
        unreadNotification.setSentAt(LocalDateTime.now());

        // An already-read notification for user 1
        readNotification = new Notification();
        readNotification.setNotificationId(2L);
        readNotification.setRecipientId(1L);
        readNotification.setType(NotificationType.ORDER_CONFIRMED);
        readNotification.setTitle("Order Confirmed");
        readNotification.setMessage("Restaurant has confirmed your order.");
        readNotification.setChannel(NotificationChannel.IN_APP);
        readNotification.setRead(true);
        readNotification.setSentAt(LocalDateTime.now().minusMinutes(5));
        readNotification.setReadAt(LocalDateTime.now().minusMinutes(2));

        // Send request DTO
        sendRequest = new SendNotificationRequest();
        sendRequest.setRecipientId(1L);
        sendRequest.setType(NotificationType.ORDER_PLACED);
        sendRequest.setTitle("Order Placed!");
        sendRequest.setMessage("Your order has been placed.");
        sendRequest.setChannel(NotificationChannel.IN_APP);
        sendRequest.setRelatedId(10L);
        sendRequest.setRelatedType("ORDER");

        // Bulk request DTO
        bulkRequest = new BulkNotificationRequest();
        bulkRequest.setRecipientIds(List.of(1L, 2L, 3L));
        bulkRequest.setType(NotificationType.PROMO);
        bulkRequest.setTitle("Weekend Offer!");
        bulkRequest.setMessage("Get 20% off this weekend!");
    }

    // Test 1: send creates and returns notification
    @Test
    void send_ShouldCreateAndReturnNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(unreadNotification);
        NotificationResponse result = notificationService.send(sendRequest);

        assertNotNull(result);
        assertEquals(1L, result.getRecipientId());
        assertEquals("Order Placed!", result.getTitle());
        assertFalse(result.isRead());
        assertEquals(NotificationType.ORDER_PLACED, result.getType());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // Test 2: sendBulk creates one notification per recipient
    @Test
    void sendBulk_ShouldCreateOneNotificationPerRecipient() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(unreadNotification);
        List<NotificationResponse> results = notificationService.sendBulk(bulkRequest);
        assertNotNull(results);
        // 3 recipients → 3 notifications saved
        assertEquals(3, results.size());
        verify(notificationRepository, times(3))
                .save(any(Notification.class));
    }

    // Test 3: getByRecipient returns all notifications
    @Test
    void getByRecipient_ShouldReturnAllNotifications() {
        when(notificationRepository
                .findByRecipientIdOrderBySentAtDesc(1L))
                .thenReturn(List.of(unreadNotification, readNotification));
        List<NotificationResponse> results = notificationService.getByRecipient(1L);
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    // Test 4: getUnreadByRecipient returns only unread
    @Test
    void getUnreadByRecipient_ShouldReturnOnlyUnread() {
        when(notificationRepository
                .findByRecipientIdAndIsReadFalse(1L)).thenReturn(List.of(unreadNotification));

        List<NotificationResponse> results = notificationService.getUnreadByRecipient(1L);

        assertEquals(1, results.size());
        assertFalse(results.get(0).isRead());
    }

    // Test 5: getUnreadCount returns correct count
    @Test
    void getUnreadCount_ShouldReturnCount() {
        when(notificationRepository
                .countByRecipientIdAndIsReadFalse(1L)).thenReturn(3L);

        long count = notificationService.getUnreadCount(1L);
        assertEquals(3L, count);
    }

    // Test 6: markAsRead marks notification as read
    @Test
    void markAsRead_ShouldMarkAsRead_WhenOwner() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));

        // After saving, return notification with isRead=true
        Notification markedRead = new Notification();
        markedRead.setNotificationId(1L);
        markedRead.setRecipientId(1L);
        markedRead.setRead(true);
        markedRead.setReadAt(LocalDateTime.now());
        markedRead.setType(NotificationType.ORDER_PLACED);
        markedRead.setTitle("Order Placed!");
        markedRead.setMessage("Your order has been placed.");
        markedRead.setChannel(NotificationChannel.IN_APP);
        markedRead.setSentAt(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(markedRead);
        NotificationResponse result = notificationService.markAsRead(1L, 1L);

        assertTrue(result.isRead());
        assertNotNull(result.getReadAt());
    }

    // Test 7: markAsRead throws when not your notification
    @Test
    void markAsRead_ShouldThrow_WhenNotOwner() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));
        // User 99 tries to mark user 1's notification as read
        assertThrows(BadRequestException.class,
                () -> notificationService.markAsRead(1L, 99L));
    }

    // Test 8: markAsRead does not save if already read
    @Test
    void markAsRead_ShouldNotSave_WhenAlreadyRead() {
        // readNotification.isRead = true (set in setUp)
        when(notificationRepository.findById(2L)).thenReturn(Optional.of(readNotification));
        notificationService.markAsRead(2L, 1L);
        // save() should NOT be called — already read
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    // Test 9: markAllAsRead returns count of updated
    @Test
    void markAllAsRead_ShouldReturnUpdatedCount() {
        // 2 notifications were marked as read
        when(notificationRepository.markAllAsRead(1L)).thenReturn(2);
        int count = notificationService.markAllAsRead(1L);
        assertEquals(2, count);
        verify(notificationRepository, times(1)).markAllAsRead(1L);
    }

    // Test 10: deleteNotification deletes when owner
    @Test
    void deleteNotification_ShouldDelete_WhenOwner() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));
        doNothing().when(notificationRepository).deleteById(1L);
        notificationService.deleteNotification(1L, 1L);
        verify(notificationRepository, times(1)).deleteById(1L);
    }

    // Test 11: deleteNotification throws when not your notification
    @Test
    void deleteNotification_ShouldThrow_WhenNotOwner() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));
        // User 99 tries to delete user 1's notification
        assertThrows(BadRequestException.class,
                () -> notificationService.deleteNotification(1L, 99L));
        // deleteById should NOT have been called
        verify(notificationRepository, never()).deleteById(any());
    }

    // Test 12: deleteNotification throws when not found
    @Test
    void deleteNotification_ShouldThrow_WhenNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.deleteNotification(999L, 1L));
    }

    // Test 13: sendOrderNotification sends with ORDER type
    @Test
    void sendOrderNotification_ShouldSendWithOrderDetails() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(unreadNotification);
        NotificationResponse result =
                notificationService.sendOrderNotification(
                        1L,
                        10L,
                        "ORDER_PLACED",
                        "Order Placed!",
                        "Your order #10 has been placed."
                );

        assertNotNull(result);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // Test 14: getAll returns all notifications
    @Test
    void getAll_ShouldReturnAllNotifications() {
        when(notificationRepository.findAll())
                .thenReturn(List.of(unreadNotification, readNotification));
        List<NotificationResponse> results = notificationService.getAll();
        assertEquals(2, results.size());
    }

    // Test 15: getUnreadCount returns zero when all read
    @Test
    void getUnreadCount_ShouldReturnZero_WhenAllRead() {

        when(notificationRepository
                .countByRecipientIdAndIsReadFalse(1L)).thenReturn(0L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(0L, count);
    }
}