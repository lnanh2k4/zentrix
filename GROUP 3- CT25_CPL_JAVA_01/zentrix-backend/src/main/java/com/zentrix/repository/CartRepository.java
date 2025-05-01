package com.zentrix.repository;

import org.springframework.data.domain.Pageable;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.Cart;
import com.zentrix.model.entity.CartProductTypeVariation;
import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.entity.User;

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date February 17, 2025
*/
public interface CartRepository extends JpaRepository<Cart, Long> {
    /**
     * This method allows to find all carts for a specific user by their user ID.
     * It retrieves a list of carts associated with the given user.
     *
     * @param userId the user whose carts are to be retrieved.
     * @return a list of Cart objects belonging to the specified user.
     */
    Page<Cart> findByUserId(User userId, Pageable pageable);

}
