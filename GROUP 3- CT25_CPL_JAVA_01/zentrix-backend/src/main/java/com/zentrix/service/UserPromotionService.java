package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Promotion;
import com.zentrix.model.entity.UserPromotion;
import com.zentrix.model.request.UserPromotionRequest;

/*
 * @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */
public interface UserPromotionService {

    /**
     * This method allows to add a new user promotion with a promotion ID
     * 
     * @param request user promotion request data
     * @param promId  ID of the promotion
     * @return the created user promotion entity
     */
    UserPromotion addUserPromotion(UserPromotionRequest request, Long promId);

    /**
     * This method allows to find all user promotions by user ID
     * 
     * @param userId ID of the user
     * @return list of user promotion entities associated with the user ID
     */
    List<UserPromotion> findAllUserPromotionByUserId(Long userId);

    /**
     * This method allows to save a user promotion with user and promotion details
     * 
     * @param userId      ID of the user
     * @param promotionId promotion entity to associate with the user
     */
    void autoClaimUserPromotion(Long userId, Promotion promotionId);

    /**
     * This method allows a user to claim a promotion
     * 
     * @param request user promotion request data
     * @param promId  ID of the promotion
     * @param userId  ID of the user claiming the promotion
     * @return the created user promotion entity
     */
    UserPromotion claimPromotion(UserPromotionRequest request, Long promId, Long userId);
}