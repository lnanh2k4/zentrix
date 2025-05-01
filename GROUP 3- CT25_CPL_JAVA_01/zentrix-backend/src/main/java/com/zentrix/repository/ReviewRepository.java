package com.zentrix.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.zentrix.model.entity.Review;
import com.zentrix.model.entity.User;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * Retrieves a list of reviews associated with a specific product type ID.
     *
     * @param prodId The ID of the product type to retrieve reviews for.
     * @return A list of reviews linked to the given product type.
     */
    List<Review> findByProduct_prodTypeId(Long prodId);

    /**
     * Retrieves a list of reviews submitted by a specific user.
     *
     * @param user The user who created the reviews.
     * @return A list of reviews made by the specified user.
     */
    List<Review> findByUser(User user);

    /**
     * Retrieves all reviews for a given product
     * where the user is active (status = 1).
     *
     * @param productId the ID of the product
     * @return list of active user reviews for the product
     */
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.user.status = 1")
    List<Review> findAllByProductIdAndActiveUser(Long productId);

}
