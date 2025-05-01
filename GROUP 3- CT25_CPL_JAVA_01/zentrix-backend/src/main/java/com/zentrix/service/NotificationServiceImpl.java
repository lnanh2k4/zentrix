package com.zentrix.service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.zentrix.model.entity.Notification;
import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.request.NotificationRequest;
import com.zentrix.model.response.NotificationResponse;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.NotificationRepository;
import com.zentrix.repository.OrderRepository;
import com.zentrix.repository.StaffRepository;
import com.zentrix.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.util.Date;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date April 09, 2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements NotificationService {

    NotificationRepository notificationRepository;
    StaffRepository staffRepository;
    OrderRepository orderRepository;
    UserRepository userRepository;
    @Override
    public PaginationWrapper<List<NotificationResponse>> searchNotifications(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository.findByTitleContainingIgnoreCase(keyword, pageable);

        List<NotificationResponse> responses = notificationPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PaginationWrapper.Builder<List<NotificationResponse>>()
                .setData(responses)
                .setPaginationInfo(notificationPage)
                .build();
    }

    @Override
    public PaginationWrapper<List<NotificationResponse>> getAllNotifications(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "notiId"));
        if (userId == null) {
            Page<Notification> notificationPage = notificationRepository.findVisibleNotificationsForUser(userId,pageable);


            List<NotificationResponse> responses = notificationPage.getContent()
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return new PaginationWrapper.Builder<List<NotificationResponse>>()
                    .setData(responses)
                    .setPaginationInfo(notificationPage)
                    .build();
        } else {
            Page<Notification> notificationPage = notificationRepository.findAll(pageable);

            List<NotificationResponse> responses = notificationPage.getContent()
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return new PaginationWrapper.Builder<List<NotificationResponse>>()
                    .setData(responses)
                    .setPaginationInfo(notificationPage)
                    .build();
        }

    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void createNotification(NotificationRequest request) {
        if (request.getCreatedById() == null) {
            throw new ValidationFailedException(AppCode.INVALID_INPUT);
        }
        Long orderId = request.getOrderId();
        User user = null;
        Boolean isPublic = true;
        if (orderId != null) {
            user = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ValidationFailedException(AppCode.USER_NOT_FOUND)).getUserId();
            isPublic = false;
        }

        User createdBy = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new ValidationFailedException(AppCode.USER_NOT_FOUND));
        Staff staff = staffRepository.findStaffByUserId(createdBy);
        Notification notification = Notification.builder()
                .createdBy(staff)
                .title(request.getTitle())
                .description(request.getDescription())
                .user(user)
                .isPublic(isPublic)
                .createdAt(request.getCreatedAt() != null
                        ? request.getCreatedAt()
                        : new Date())
                .status(request.getStatus())
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public NotificationResponse getNotificationById(Long notiId) {
        Notification notification = notificationRepository.findById(notiId)
                .orElse(null);
        if (notification == null) {
            return null;
        }
        return mapToResponse(notification);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void deleteNotification(Long notiId) {
        Notification existingNotification = notificationRepository.findById(notiId)
                .orElseThrow(() -> new ValidationFailedException(AppCode.NOTIFICATION_NOT_FOUND));

        try {

            notificationRepository.delete(existingNotification);
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.NOTIFICATION_DELETE_FAILED);
        }
    }

    @Override
    public NotificationResponse mapToResponse(Notification notification) {
        if (notification == null) {
            return NotificationResponse.builder()
                    .notiId(0L)
                    .title("No Notification")
                    .description("No description available.")
                    .createdAt(new java.util.Date())
                    .status(0)
                    .createdByName("Unknown")
                    .build();
        }

        String createdByName = "Unknown";
        if (notification.getCreatedBy() != null && notification.getCreatedBy().getUserId() != null) {
            createdByName = notification.getCreatedBy().getUserId().getUsername();
        }

        return NotificationResponse.builder()
                .notiId(notification.getNotiId())
                .title(notification.getTitle())
                .description(notification.getDescription())
                .createdAt(notification.getCreatedAt())
                .status(notification.getStatus())
                .createdByName(createdByName)
                .userId(notification.getUser() != null ? notification.getUser().getUserId() : null)
                .build();
    }

    @Override
    public void updateNotificationStatus(Long id) {
    Notification noti = notificationRepository.findById(id).orElseThrow(()-> new ActionFailedException(AppCode.NOTIFICATION_DELETE_FAILED) );
    if(noti.getStatus() == 0) {
        noti.setStatus(1);
    }else {
        noti.setStatus(0);
    }
    notificationRepository.save(noti);
    }

}
