package com.zentrix.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.zentrix.model.entity.Warranty;
import com.zentrix.model.request.WarrantyRequest;

/*
 * @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */
public interface WarrantyService {

    /**
     * This method allows to add a new warranty with the creator's ID
     * 
     * @param request     warranty request data
     * @param createdById ID of the user creating the warranty
     * @return the created warranty entity
     */
    Warranty addWarranty(WarrantyRequest request, Long createdById);

    /**
     * This method allows to find a warranty by its ID
     * 
     * @param warnId ID of the warranty
     * @return the warranty entity if found
     */
    Warranty findWarrantyById(Long warnId);

    /**
     * This method allows to delete a warranty by its ID
     * 
     * @param warnId ID of the warranty to delete
     */
    void deleteWarranty(Long warnId);

    /**
     * This method allows to update an existing warranty
     * 
     * @param request warranty request data
     * @param warnId  ID of the warranty to update
     * @return the updated warranty entity
     */
    Warranty updateWarranty(WarrantyRequest request, Long warnId);

    /**
     * This method allows to get all warranties with pagination
     * 
     * @param pageable pageable of list
     * @return page of warranty entities
     */
    Page<Warranty> findAllWarranty(Pageable pageable);

    /**
     * This method allows to find warranties by user's phone number with pagination
     * 
     * @param phone    phone number of the user
     * @param pageable pageable of list
     * @return page of warranty entities associated with the phone number
     */
    Page<Warranty> findWarrantyByUserPhone(String phone, Pageable pageable);
}