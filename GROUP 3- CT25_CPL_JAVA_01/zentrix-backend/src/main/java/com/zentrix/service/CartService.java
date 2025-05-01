package com.zentrix.service;

import com.zentrix.model.entity.Cart;
import com.zentrix.model.entity.CartProductTypeVariation;
import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.entity.User;
import com.zentrix.model.response.PaginationWrapper;

import java.util.List;

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date February 13, 2025
*/
public interface CartService {
    /**
     * Retrieves carts for a user with pagination.
     * 
     * @param userId The user to fetch carts for.
     * @param page   The page number.
     * @param size   The number of items per page.
     * @return A paginated list of carts.
     */
    PaginationWrapper<List<Cart>> getCartByUserId(User userId, int page, int size);

    /**
     * Creates a new cart for a user.
     * 
     * @param user The user to create a cart for.
     * @return The created cart.
     */
    Cart createCart(User user);

    /**
     * Retrieves a cart by its ID.
     * 
     * @param id The ID of the cart.
     * @return The cart with the specified ID.
     */
    Cart getCartById(Long id);

    /**
     * Adds a product to a user's cart.
     * 
     * @param cartId                 The ID of the cart.
     * @param productTypeVariationId The ID of the product variation.
     * @param quantity               The quantity to add.
     * @param user                   The user who owns the cart.
     * @return The updated cart.
     */
    Cart addProductToCart(Long cartId, Long productTypeVariationId, Integer quantity, User user, String variCode);

    /**
     * Clears all items from a user's cart.
     * 
     * @param cartId The ID of the cart.
     * @param user   The user who owns the cart.
     */
    void clearCart(Long cartId, User user);

    /**
     * Retrieves all items in a user's cart.
     * 
     * @param cartId The ID of the cart.
     * @param user   The user who owns the cart.
     * @return A list of cart items.
     */
    List<CartProductTypeVariation> getCartItems(Long cartId, User user);

}
