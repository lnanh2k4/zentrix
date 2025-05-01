package com.zentrix.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.NotificationRequest;
import com.zentrix.model.response.NotificationResponse;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date April 01, 2025
 */
@Slf4j
@RequestMapping("/api/v1/dashboard/notifications")
@RestController
@Tag(name = "Notification Dashboard Controller", description = "Controller for managing notifications.")
public class NotificationDashboardController {
@Autowired
    private NotificationService notificationService;

    /**
     * Retrieve all notifications.
     *
     * @param jwt Authorization token
     * @return List of notifications
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "get notifications", description = "Get all notifications")
    @GetMapping("")
    public ResponseEntity<PaginationWrapper<List<NotificationResponse>>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.getAllNotifications(page,
                size, null));
    }

/**
 * Updates the status of a specific notification by its ID.
 *
 * @param id The ID of the notification to update.
 * @return ResponseEntity indicating whether the operation was successful.
 */
 @Operation(summary = "get notifications", description = "Get all notifications")
    @PostMapping("/{id}")
    public ResponseEntity<ResponseObject<NotificationResponse>> updateNotificationStatus(@PathVariable(name = "id") Long id) {
        notificationService.updateNotificationStatus(id);
        ResponseObject<NotificationResponse> response = new ResponseObject.Builder<NotificationResponse>()
                .message("Successfully")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }


    /**
     * Search for notifications by title keyword.
     *
     * @param keyword The keyword used for searching
     * @param jwt     Authorization token
     * @return List of notifications matching the search criteria
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search notifications", description = "Search for notifications by their title keyword.")
    @GetMapping("/search")
    public ResponseEntity<PaginationWrapper<List<NotificationResponse>>> searchNotifications(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.searchNotifications(keyword,
                page, size));
    }

    /**
     * Retrieve a specific notification by its ID.
     *
     * @param notiId The ID of the notification
     * @param jwt    Authorization token
     * @return The requested notification details
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notification by ID", description = "Retrieve details of a specific notification using its ID.")
    @GetMapping("/{notiId}")
    public ResponseEntity<ResponseObject<NotificationResponse>> getNotificationById(@PathVariable Long notiId) {
        NotificationResponse notification = notificationService.getNotificationById(notiId);

        ResponseObject<NotificationResponse> response = new ResponseObject.Builder<NotificationResponse>()
                .content(notification)
                .message("Notification retrieved successfully by ID.")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Create a new notification.
     *
     * @param request The notification request data
     * @param jwt     Authorization token
     * @return Success message
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new notification", description = "Add a new notification to the system.")
    @PostMapping("/add")
    public ResponseEntity<ResponseObject<?>> createNotification(
            @Valid @RequestBody NotificationRequest notificationRequest,
            BindingResult bindingResult) {
            Map<String, String> errors = new HashMap<>();

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(e -> {
                errors.put(e.getField(), e.getDefaultMessage());
                log.error("Validation errors - {}: {}", e.getField(), e.getDefaultMessage());
            });
        }

        if (notificationRequest.getTitle() != null &&
                notificationRequest.getTitle().trim().isEmpty()) {
            errors.put("title", AppCode.NOTIFICATION_TITLE_BLANK.getMessage());
        } else if (notificationRequest.getTitle().length() < 5 ||
                notificationRequest.getTitle().length() > 100) {
            errors.put("title", AppCode.NOTIFICATION_TITLE_INVALID_LENGTH.getMessage());
        }

        if (notificationRequest.getDescription() != null &&
                notificationRequest.getDescription().trim().isEmpty()) {
            errors.put("description",
                    AppCode.NOTIFICATION_DESCRIPTION_BLANK.getMessage());
        } else if (notificationRequest.getDescription().length() < 10 ||
                notificationRequest.getDescription().length() > 100) {
            errors.put("description",
                    AppCode.NOTIFICATION_DESCRIPTION_INVALID_LENGTH.getMessage());
        }

        if (notificationRequest.getCreatedAt() == null) {
            notificationRequest.setCreatedAt(new java.sql.Date(System.currentTimeMillis()));
        } else if (notificationRequest.getCreatedAt().after(new Date())) {
            errors.put("createdAt", AppCode.NOTIFICATION_CREATED_AT_IN_FUTURE.getMessage());
        }

        if (notificationRequest.getStatus() == null) {
            errors.put("status", AppCode.NOTIFICATION_STATUS_REQUIRED.getMessage());
        } else if (notificationRequest.getStatus() != 0 &&
                notificationRequest.getStatus() != 1) {
            errors.put("status", AppCode.NOTIFICATION_STATUS_INVALID.getMessage());
        }

        if (!errors.isEmpty()) {
            log.warn("Validation failed: {}", errors);
            return ResponseEntity.badRequest().body(
                    new ResponseObject.Builder<Map<String, String>>()
                            .code(AppCode.FIELD_NOT_VALID.getCode())
                            .success(false)
                            .message("Validation failed")
                            .content(errors)
                            .build());
        }

        try {
            notificationService.createNotification(notificationRequest);
            return ResponseEntity.ok(
                    new ResponseObject.Builder<Void>()
                            .code(200)
                            .success(true)
                            .message("Notification created successfully")
                            .content(null)
                            .build());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ResponseObject.Builder<String>()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .message(ex.getMessage())
                            .content(null)
                            .build());
        }
    }

    /**
     * Delete a notification by ID.
     *
     * @param notiId The ID of the notification to be deleted
     * @param jwt    Authorization token
     * @return Success message
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a notification", description = "Remove a notification from the system using its ID.")
    @DeleteMapping("/remove/{notiId}")
    public ResponseEntity<ResponseObject<?>> deleteNotification(@PathVariable Long notiId
    // @RequestHeader("Authorization") String jwt
    ) {
        try {
            notificationService.deleteNotification(notiId);
            ;
            return ResponseEntity.ok(
                    new ResponseObject.Builder<Void>()
                            .code(200)
                            .success(true)
                            .message("Notification delete successfully")
                            .content(null)
                            .build());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ResponseObject.Builder<String>()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .message(ex.getMessage())
                            .content(null)
                            .build());
        }
    }


    
}
