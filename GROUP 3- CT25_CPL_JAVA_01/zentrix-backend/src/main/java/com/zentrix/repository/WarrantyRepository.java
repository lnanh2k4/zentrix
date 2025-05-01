package com.zentrix.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zentrix.model.entity.Warranty;

/*
 * @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */
public interface WarrantyRepository extends JpaRepository<Warranty, Long> {

    /**
     * This method allows to get all warranties with pagination
     * 
     * @param pageable pageable of list
     * @return page of warranty entities
     */
    Page<Warranty> findAll(Pageable pageable);

    /**
     * This method allows to find warranties by user's phone number
     * 
     * @param phone    phone number of user
     * @param pageable pageable of list
     * @return page of warranty entities associated with the phone number
     */
    @Query("SELECT w FROM Warranty w WHERE w.userId.phone = :phone")
    Page<Warranty> findByUserPhone(@Param("phone") String phone, Pageable pageable);
}