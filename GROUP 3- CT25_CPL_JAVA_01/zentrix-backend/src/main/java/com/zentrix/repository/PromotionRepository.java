package com.zentrix.repository;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zentrix.model.entity.Promotion;

/*
 * @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * This method allows to search promotions by keyword or ID
     * 
     * @param keyword  keyword to search promotion name or code
     * @param id       promotion ID (optional)
     * @param pageable pageable of list
     * @return page of promotion entities matching the search criteria
     */
    @Query(value = "SELECT p FROM Promotion p WHERE LOWER(p.promName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR p.promCode LIKE CONCAT('%', :keyword, '%') " +
            "OR (:id IS NOT NULL AND p.promId = :id)")
    Page<Promotion> findPromotion(@Param("keyword") String keyword, @Param("id") Long promId, Pageable pageable);

    /**
     * This method allows to find all active promotions based on current date and
     * status
     * 
     * @param pageable pageable of list
     * @return page of active promotion entities
     */
    @Query("SELECT p FROM Promotion p WHERE " +
            "((p.promName NOT LIKE '%Promotion' AND p.approvedBy IS NOT NULL) OR " +
            "(p.promName LIKE '%Promotion')) AND " +
            "CAST(p.startDate AS DATE) <= CURRENT_DATE AND " +
            "CAST(p.endDate AS DATE) >= CURRENT_DATE AND " +
            "p.promStatus = 1")
    Page<Promotion> findActivePromotions(Pageable pageable);

    /**
     * This method allows to find all inactive promotions based on current date or
     * status
     * 
     * @param pageable pageable of list
     * @return page of inactive promotion entities
     */
    @Query("SELECT p FROM Promotion p WHERE " +
            "((p.promName NOT LIKE '%Promotion' AND (p.approvedBy IS NULL OR " +
            "CAST(p.startDate AS DATE) > CURRENT_DATE OR " +
            "CAST(p.endDate AS DATE) < CURRENT_DATE OR " +
            "p.promStatus != 1)) OR " +
            "(p.promName LIKE '%Promotion' AND (" +
            "CAST(p.startDate AS DATE) > CURRENT_DATE OR " +
            "CAST(p.endDate AS DATE) < CURRENT_DATE OR " +
            "p.promStatus != 1)))")
    Page<Promotion> findInactivePromotions(Pageable pageable);

    /**
     * This method allows to find active promotions by a specific date
     * 
     * @param date     date to check active promotions
     * @param pageable pageable of list
     * @return page of promotion entities active on the specified date
     */
    @Query("SELECT p FROM Promotion p WHERE CAST(p.startDate AS DATE) <= :date AND CAST(p.endDate AS DATE) >= :date AND p.promStatus = 1")
    Page<Promotion> findActivePromotionsByDate(@Param("date") Date date, Pageable pageable);

    /**
     * This method allows to find a promotion by its promotion code
     * 
     * @param promCode promotion code
     * @return optional promotion entity if found
     */
    Optional<Promotion> findByPromCode(String promCode);

    /**
     * This method checks if a promotion code already exists
     * 
     * @param promCode promotion code
     * @return true if the promotion code exists, false otherwise
     */
    boolean existsByPromCode(String promCode);
}