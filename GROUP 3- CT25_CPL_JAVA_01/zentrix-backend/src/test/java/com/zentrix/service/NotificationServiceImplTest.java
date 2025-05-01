package com.zentrix.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zentrix.model.entity.Notification;
import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.request.NotificationRequest;
import com.zentrix.model.response.NotificationResponse;
import com.zentrix.repository.NotificationRepository;
import com.zentrix.repository.OrderRepository;
import com.zentrix.repository.StaffRepository;
import com.zentrix.repository.UserRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private NotificationServiceImpl notificationService;

    private NotificationRequest request;
    private Notification notification;
    private Staff staff;
    private User user;

    @BeforeEach
    void setUp() {
        staff = new Staff();
        staff.setStaffId(1L);

        user = new User();
        user.setUserId(1L);
        user.setUsername("admin");

        request = new NotificationRequest();
        request.setCreatedById(1L);
        request.setTitle("Maintenance");
        request.setDescription("System will be down");
        request.setCreatedAt(new Date(System.currentTimeMillis()));
        request.setStatus(0);

        notification = Notification.builder()
                .notiId(10L)
                .title("Maintenance")
                .description("System will be down")
                .status(0)
                .createdAt(new Date(System.currentTimeMillis()))
                .createdBy(staff)
                .build();
    }

 
    @Test
    void testGetNotificationById_Success() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        NotificationResponse result = notificationService.getNotificationById(10L);
        assertEquals("Maintenance", result.getTitle());
    }

    @Test
    void testGetNotificationById_NotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        NotificationResponse result = notificationService.getNotificationById(99L);
        assertNull(result);
    }

    @Test
    void testCreateNotification_Success_Public() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(staffRepository.findStaffByUserId(user)).thenReturn(staff);
        notificationService.createNotification(request);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateNotification_Fail_NoCreatedBy() {
        request.setCreatedById(null);
        assertThrows(ValidationFailedException.class, () -> notificationService.createNotification(request));
    }

    @Test
    void testCreateNotification_Fail_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ValidationFailedException.class, () -> notificationService.createNotification(request));
    }

    @Test
    void testSearchNotifications_Success() {
        when(notificationRepository.findByTitleContainingIgnoreCase(eq("maint"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(notification)));
        List<NotificationResponse> result = notificationService.searchNotifications("maint", 0, 10).getData();
        assertEquals(1, result.size());
        assertEquals("Maintenance", result.get(0).getTitle());
    }

    @Test
    void testDeleteNotification_Success() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        notificationService.deleteNotification(10L);
        verify(notificationRepository, times(1)).delete(notification);
    }

    @Test
    void testDeleteNotification_NotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ValidationFailedException.class, () -> notificationService.deleteNotification(99L));
    }

    @Test
    void testUpdateNotificationStatus_ToggleFrom0to1() {
        notification.setStatus(0);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        notificationService.updateNotificationStatus(10L);
        assertEquals(1, notification.getStatus());
    }

    @Test
    void testUpdateNotificationStatus_ToggleFrom1to0() {
        notification.setStatus(1);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        notificationService.updateNotificationStatus(10L);
        assertEquals(0, notification.getStatus());
    }

    @Test
    void testUpdateNotificationStatus_NotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ActionFailedException.class, () -> notificationService.updateNotificationStatus(99L));
    }
}
