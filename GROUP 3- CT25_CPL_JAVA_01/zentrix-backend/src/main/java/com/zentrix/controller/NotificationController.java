package com.zentrix.controller;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zentrix.model.response.NotificationResponse;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/*
* @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
* @date February 13, 2025
*/
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Controller", description = "Controller for managing notifications.")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieve all notifications.
     *
     * @param jwt Authorization token
     * @return List of notifications
     */
    @PreAuthorize("permitAll()")
    @Operation(summary = "get notifications", description = "Get all notifications")
    @GetMapping("/get-all/{id}")
    public ResponseEntity<PaginationWrapper<List<NotificationResponse>>> getAllNotifications(
            @PathVariable(name = "id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "999") int size) {
        return ResponseEntity.ok(notificationService.getAllNotifications(page,
                size,userId));
    }

    /**
     * Search for notifications by title keyword.
     *
     * @param keyword The keyword used for searching
     * @param jwt     Authorization token
     * @return List of notifications matching the search criteria
     */
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
    @PreAuthorize("permitAll()")
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
}
