package com.zentrix.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.zentrix.model.entity.Notification;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Search for notifications that contain the specified keyword in their title,
     * ignoring case sensitivity.
     *
     * @param keyword The keyword used for searching in the title.
     * @return A list of notifications that contain the keyword in their title.
     */
    Page<Notification> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    /**
     * Retrieves visible notifications for a user,
     * including global (user is null) and user-specific ones.
     *
     * @param userId   the user's ID
     * @param pageable pagination information
     * @return page of active notifications for the user
     */
    @Query("SELECT n FROM Notification n " +
            "WHERE (n.user IS NULL OR n.user.userId = :userId) " +
            "AND n.status = 1")
    Page<Notification> findVisibleNotificationsForUser(@Param("userId") Long userId, Pageable pageable);

}
