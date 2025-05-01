package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.OrderDetail;
import com.zentrix.model.request.OrderDetailRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date February 17, 2025
*/
public interface OrderDetailService {

    /**
     * Saves an order detail into the database.
     *
     * @param orderDetail The OrderDetail object to be saved.
     * @throws AppException If an error occurs during the save process, e.g., a
     *                      database error.
     */
    OrderDetail saveOrderDetail(OrderDetailRequest orderDetailRequest);

    /**
     * Finds an order detail by its ID.
     *
     * @param ordtId The ID of the order detail.
     * @return The OrderDetail object associated with the given ID.
     * @throws AppException If the order detail is not found or an error occurs
     *                      during the query.
     */
    OrderDetail findOrderDetail(long ordtId);

    /**
     * Updates an order detail in the database.
     *
     * @param orderDetail The OrderDetail object containing the data to be
     *                    updated.
     * @throws AppException If an error occurs during the update process, e.g.,
     *                      the
     *                      order detail does not exist.
     */
    void updateOrderDetail(OrderDetail orderDetail);

    /**
     * Deletes an order detail by its ID.
     *
     * @param ordtId The ID of the order detail to be deleted.
     * @throws AppException If the order detail is not found or an error occurs
     *                      during deletion.
     */
    void deleteOrderDetail(long ordtId);

    /**
     * Retrieves a list of all order details.
     *
     * @return A list of all OrderDetail objects.
     * @throws AppException If an error occurs during the data retrieval process.
     */
    PaginationWrapper<List<OrderDetail>> findAllOrderDetail(int page, int size);

    /**
     * Finds all order details by a specific order ID.
     *
     * @param orderId The Order object containing the order ID.
     * @return A list of OrderDetail objects associated with the given order ID.
     * @throws AppException If an error occurs during the query process.
     */
    PaginationWrapper<List<OrderDetail>> findByOrderID(Order orderId, int page, int size);

    /**
     * 
     * @param orderId
     */
    public void restoreQuantitiesForOrder(Long orderId);
}
