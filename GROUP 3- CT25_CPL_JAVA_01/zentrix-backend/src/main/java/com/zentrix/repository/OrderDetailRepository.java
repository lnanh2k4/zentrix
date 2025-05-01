package com.zentrix.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.OrderDetail;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
       /**
        * This method allows to find all order details for a specific order by its
        * order ID.
        * It retrieves a list of OrderDetail objects associated with the given order.
        *
        * @param orderId the order whose details are to be retrieved.
        * @return a list of OrderDetail objects belonging to the specified order.
        */
       Page<OrderDetail> findByOrderId(Order orderId, Pageable pageable);

       /**
        * Retrieves order details for a specific user.
        * 
        * @param userId The ID of the user.
        * @return List of order details.
        */
       @Query("""
                     SELECT od
                     FROM OrderDetail od
                     JOIN od.orderId o
                     WHERE o.userId.userId = :userId
                     """)
       List<OrderDetail> findByUserId(@Param("userId") Long userId);
}
