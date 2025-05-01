package com.zentrix.repository;

import com.zentrix.model.entity.Cart;
import com.zentrix.model.entity.CartProductTypeVariation;
import com.zentrix.model.entity.ProductTypeVariation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartProductTypeVariationRepository extends JpaRepository<CartProductTypeVariation, Long> {
    /**
     * Finds a cart item by cart and product type variation.
     * 
     * @param cart                 The cart to search in.
     * @param productTypeVariation The product type variation to search for.
     * @return An Optional containing the found cart item, or empty if not found.
     */
    Optional<CartProductTypeVariation> findByCartIdAndProdTypeVariId(Cart cart,
            ProductTypeVariation productTypeVariation);

    CartProductTypeVariation findByCartIdAndProdTypeVariIdAndVariCode(Cart cartId, ProductTypeVariation prodTypeVariId,
            String variCode);
}