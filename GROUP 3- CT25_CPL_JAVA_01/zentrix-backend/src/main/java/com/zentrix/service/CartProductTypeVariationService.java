package com.zentrix.service;

import com.zentrix.model.entity.Cart;
import com.zentrix.model.entity.CartProductTypeVariation;
import com.zentrix.model.entity.ProductTypeVariation;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date April 06, 2025
 */
public interface CartProductTypeVariationService {

    /**
     * Adds a product to the cart with the specified quantity.
     * 
     * @param cart                 The cart to add the product to.
     * @param productTypeVariation The product type variation to add.
     * @param quantity             The quantity of the product.
     * @return The added cart item.
     */
    CartProductTypeVariation addToCart(Cart cart, ProductTypeVariation productTypeVariation, Integer quantity,
            String variCode);

    /**
     * Updates the quantity of a cart item.
     * 
     * @param cartProductTypeVariationId The ID of the cart item to update.
     * @param newQuantity                The new quantity to set.
     * @return The updated cart item.
     */
    CartProductTypeVariation updateQuantity(Long cartProductTypeVariationId, Integer newQuantity);

    /**
     * Removes a product from the cart.
     * 
     * @param cartProductTypeVariationId The ID of the cart item to remove.
     * @return true if the item was removed successfully, false otherwise.
     */
    boolean removeFromCart(Long cartProductTypeVariationId);
}