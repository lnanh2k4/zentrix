package com.zentrix.service;

import java.util.Date;
import java.util.List;

import com.zentrix.model.entity.Promotion;
import com.zentrix.model.request.PromotionRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */
public interface PromotionService {

    /**
     * This method allows to add a new promotion with the user ID of the creator
     * 
     * @param userId  ID of the user creating the promotion
     * @param request promotion request data
     * @return the created promotion entity
     */
    Promotion updatePromotion(Long userId, PromotionRequest request);

    /**
     * This method allows to create a new promotion
     * 
     * @param request promotion request data
     * @return the created promotion entity
     */
    Promotion createPromotion(PromotionRequest request);

    /**
     * This method allows to find a promotion by its ID
     * 
     * @param promId ID of the promotion
     * @return the promotion entity if found
     */
    Promotion findPromotionById(Long promId);

    /**
     * This method allows to delete a promotion by its ID
     * 
     * @param promId ID of the promotion to delete
     * @return the deleted promotion entity
     */
    Promotion deletePromotion(Long promId);

    /**
     * This method allows to get a paginated list of promotions
     * 
     * @param page page number
     * @param size number of items per page
     * @param sort sorting criteria
     * @return a wrapper containing the paginated list of promotions
     */
    PaginationWrapper<List<Promotion>> getPromotions(int page, int size, String sort);

    /**
     * This method allows to search promotions by keyword with pagination
     * 
     * @param keyword keyword to search promotions
     * @param page    page number
     * @param size    number of items per page
     * @param sort    sorting criteria
     * @return a wrapper containing the paginated list of matching promotions
     */
    PaginationWrapper<List<Promotion>> searchPromotion(String keyword, int page, int size, String sort);

    /**
     * This method allows to filter promotions by status and date with pagination
     * 
     * @param status status of promotions (e.g., active, inactive)
     * @param date   date to filter promotions
     * @param page   page number
     * @param size   number of items per page
     * @param sort   sorting criteria
     * @return a wrapper containing the paginated list of filtered promotions
     */
    PaginationWrapper<List<Promotion>> filterPromotions(String status, Date date, int page, int size, String sort);

    /**
     * This method checks if a promotion code already exists
     * 
     * @param promCode promotion code to check
     * @return true if the promotion code exists, false otherwise
     */
    boolean existsByPromCode(String promCode);
}