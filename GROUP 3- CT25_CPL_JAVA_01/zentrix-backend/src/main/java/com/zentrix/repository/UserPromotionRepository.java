package com.zentrix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.UserPromotion;

/*
 * @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */
public interface UserPromotionRepository extends JpaRepository<UserPromotion, Long> {

    /**
     * This method allows to find all user promotions by user ID
     * 
     * @param userId ID of the user
     * @return list of user promotion entities associated with the user ID
     */
    List<UserPromotion> findAllByUserIdUserId(Long userId);
}