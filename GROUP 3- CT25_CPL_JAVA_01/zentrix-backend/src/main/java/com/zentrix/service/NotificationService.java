package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Notification;
import com.zentrix.model.request.NotificationRequest;
import com.zentrix.model.response.NotificationResponse;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
public interface NotificationService {

    /**
     * Creates a new notification.
     *
     * @param request The request object containing notification details.
     */
    void createNotification(NotificationRequest request);

    /**
     * Retrieves a notification by its ID.
     *
     * @param notiId The ID of the notification to retrieve.
     * @return The notification details as a NotificationResponse object.
     */
    NotificationResponse getNotificationById(Long notiId);

    /**
     * Deletes a notification by its ID.
     *
     * @param notiId The ID of the notification to delete.
     */
    void deleteNotification(Long notiId);

    /**
     * Searches for notifications by title keyword.
     *
     * @param keyword The keyword used for searching in notification titles.
     * @return A list of notifications that match the search criteria.
     */
    PaginationWrapper<List<NotificationResponse>> searchNotifications(String keyword, int page, int size);

    /**
     * Retrieves a list of all notifications.
     *
     * @return A list of all notifications.
     */
    PaginationWrapper<List<NotificationResponse>> getAllNotifications(int page, int size, Long userId);

    /**
     * Maps a Notification entity to a NotificationResponse DTO.
     *
     * @param notification The notification entity to be mapped.
     * @return The mapped NotificationResponse object.
     */
    NotificationResponse mapToResponse(Notification notification);

    /**
     * Updates the status of a specific notification to mark it as read.
     *
     * @param id The ID of the notification to update.
     */
    void updateNotificationStatus(Long id);
}
