package com.zentrix.repository;

import com.zentrix.model.entity.Post;
import com.zentrix.model.utils.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 12, 2025
 */

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    /**
     * Retrieves a paginated list of posts filtered by their status.
     *
     * @param status   The status of the posts to filter by (e.g., ACTIVE, PENDING).
     * @param pageable Pagination information including page number, size, and
     *                 sorting.
     * @return A page of posts matching the given status.
     */
    Page<Post> findByStatus(Status status, Pageable pageable);

    /**
     * Retrieves a paginated list of posts filtered by status, and only those that
     * have been approved.
     *
     * @param status   The status of the posts to filter by.
     * @param pageable Pagination information including page number, size, and
     *                 sorting.
     * @return A page of posts that match the given status and have been approved
     *         (i.e., approvedBy is not null).
     */
    Page<Post> findByStatusAndApprovedByIsNotNull(Status status, Pageable pageable);

}
