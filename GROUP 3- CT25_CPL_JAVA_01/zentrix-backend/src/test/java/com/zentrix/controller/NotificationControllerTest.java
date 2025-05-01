package com.zentrix.controller;

import com.zentrix.model.response.NotificationResponse;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date April 07, 2025
 */

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();

        notificationResponse = new NotificationResponse();
        notificationResponse.setNotiId(1L);
        notificationResponse.setTitle("System Maintenance");
        notificationResponse.setDescription("Scheduled system maintenance at 12 AM.");
    }

    @Test
    void testGetAllNotifications_Success() throws Exception {
        List<NotificationResponse> notifications = Arrays.asList(notificationResponse);
        PaginationWrapper<List<NotificationResponse>> wrapper = new PaginationWrapper.Builder<List<NotificationResponse>>()
                .setData(notifications)
                .setPage(0)
                .setSize(10)
                .setTotalPages(1)
                .setTotalElements(1)
                .build();

        when(notificationService.getAllNotifications(0, 10, 1L)).thenReturn(wrapper);

        mockMvc.perform(get("/api/v1/notifications/get-all/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("System Maintenance"))
                .andExpect(jsonPath("$.data[0].description").value("Scheduled system maintenance at 12 AM."));

        verify(notificationService, times(1)).getAllNotifications(0, 10, 1L);
    }

    @Test
    void testGetNotificationById_Success() throws Exception {
        when(notificationService.getNotificationById(1L)).thenReturn(notificationResponse);

        mockMvc.perform(get("/api/v1/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification retrieved successfully by ID."))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.content.title").value("System Maintenance"));

        verify(notificationService, times(1)).getNotificationById(1L);
    }

    @Test
    void testSearchNotifications_Success() throws Exception {
        List<NotificationResponse> notifications = Arrays.asList(notificationResponse);
        PaginationWrapper<List<NotificationResponse>> wrapper = new PaginationWrapper.Builder<List<NotificationResponse>>()
                .setData(notifications)
                .setPage(0)
                .setSize(10)
                .setTotalPages(1)
                .setTotalElements(1)
                .build();

        when(notificationService.searchNotifications("System", 0, 10)).thenReturn(wrapper);

        mockMvc.perform(get("/api/v1/notifications/search")
                        .param("keyword", "System")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("System Maintenance"))
                .andExpect(jsonPath("$.data[0].description").value("Scheduled system maintenance at 12 AM."));

        verify(notificationService, times(1)).searchNotifications("System", 0, 10);
    }
}