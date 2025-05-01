package com.zentrix.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.User;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/

public interface StaffRepository extends JpaRepository<Staff, Long> {
    /**
     * This method allows to find STAFF by identity of user
     * 
     * @param userId identity of user
     * @return information of staff
     */
    Staff findStaffByUserId(User userId);

    /**
     * This method allows to search staffs by keyword
     * 
     * @param keyword  keyword to search staff
     * @param pageable pageable of list
     * @param status   status of staff that does not take
     * @param role     role of staff
     * @return staffs is pageable
     */
    @Query(value = "SELECT s FROM Staff s WHERE (LOWER(s.userId.firstName) LIKE CONCAT('%',LOWER(:keyword),'%') OR LOWER(s.userId.lastName) LIKE CONCAT('%',LOWER(:keyword),'%')) AND s.userId.status != :status AND LOWER(s.userId.roleId.roleName) != LOWER(:role) ORDER BY s.userId.userId DESC")
    Page<Staff> searchStaffs(@Param("keyword") String keyword, Pageable pageable, @Param("status") Integer status,
            @Param("role") String role);

    /**
     * This method allows to get all staffs
     * 
     * @param pageable pageable of list
     * @param status   status of staff that does not take
     * @param role     role of staff
     * @return staffs is pageable
     */
    @Query("SELECT s FROM Staff s WHERE s.userId.status != :status AND s.userId.roleId.roleName != :role ORDER BY s.userId.userId DESC")
    Page<Staff> findAll(Pageable pageable, @Param("status") Integer status, @Param("role") String role);

}
