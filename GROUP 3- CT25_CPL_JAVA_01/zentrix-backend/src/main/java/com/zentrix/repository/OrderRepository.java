package com.zentrix.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.User;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * This method allows to find all orders for a specific user based on the user's
     * ID.
     * 
     * @param userId the user whose orders are to be retrieved.
     * @return a list of orders associated with the specified user.
     */
    Page<Order> findByUserId(User userId, Pageable pageable);

    /**
     * This method allows searching for orders based on various criteria such as
     * order ID,
     * delivery address, order status, or payment method.
     * The search is done using a keyword which is matched against these fields.
     * 
     * @param keyword the keyword to search in order details, address, status, or
     *                payment method.
     * @return a list of orders that match the search criteria.
     */
    @Query("SELECT o FROM Order o " +
            "WHERE CAST(o.orderId AS string) LIKE CONCAT('%',:keyword,'%') " +
            "OR o.address LIKE CONCAT('%',:keyword,'%') " +
            "OR o.paymentMethod LIKE CONCAT('%',:keyword,'%') " + "OR o.userId.lastName LIKE CONCAT('%',:keyword,'%')"
            +
            "OR o.userId.firstName LIKE CONCAT('%',:keyword,'%')")
    Page<Order> searchOrder(@Param("keyword") String keyword, Pageable pageable);


    Optional<List<Order>> findByUserId(User userId);
}
