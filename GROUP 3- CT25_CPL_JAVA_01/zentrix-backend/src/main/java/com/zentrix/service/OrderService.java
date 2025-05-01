package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.User;
import com.zentrix.model.request.OrderRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
public interface OrderService {

    /**
     * Creates and saves a new order in the database.
     * 
     * @param orderRequest The request object containing order details.
     * @return The saved Order entity.
     */
    Order updateOrder(Long orderId, OrderRequest orderRequest);

    /**
     * Creates a new order.
     * 
     * @param orderRequest The order data.
     * @param user         The user who made the order.
     * @param promotion    The promotion applied to the order.
     * @param branch       The branch where the order is placed.
     * @return Created Order object.
     */
    Order addOrder(OrderRequest orderRequest);

    /**
     * Finds an order by its ID.
     * 
     * @param orderId The ID of the order to retrieve.
     * @return The order entity if found, otherwise null or an exception may be
     *         thrown.
     */
    Order findOrderById(Long orderId);

    /**
     * Retrieves a list of all orders.
     * 
     * @return A list of all order entities.
     */
    PaginationWrapper<List<Order>> findAllOrders(int page, int size);

    /**
     * Searches for orders based on a search term.
     * 
     * @param searchTerm The keyword or phrase used to search for orders.
     * @return A list of orders matching the search term.
     */
    PaginationWrapper<List<Order>> searchOrder(String searchTerm, int page, int size);

    /**
     * Retrieves all orders placed by a specific user.
     *
     * @param userId The user ID for which to retrieve orders.
     * @return A list of orders placed by the user.
     */
    PaginationWrapper<List<Order>> findOrdersByUser(User userId, int page, int size);
}
