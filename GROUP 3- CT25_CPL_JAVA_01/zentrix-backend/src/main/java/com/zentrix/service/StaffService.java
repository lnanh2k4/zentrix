package com.zentrix.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.zentrix.model.entity.Staff;
import com.zentrix.model.response.PaginationWrapper;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 13, 2025
*/
public interface StaffService {
    /**
     * This method allows to create a new staff
     *
     * @param staff information of staff
     * @return Staff information
     */
    Staff createStaff(Staff staff);

    /**
     * This method allows to update staff
     *
     * @param staff information of staff
     * @return Staff information
     */
    Staff updateStaff(Staff staff);

    /**
     * This method allows to get staffs list
     * 
     * @param page page of list
     * @param size size of list
     * @return staffs list
     */
    PaginationWrapper<List<Staff>> getAllStaffs(int page, int size);

    /**
     * This method allows to find staff by staff id
     *
     * @param staffId identity of staff
     * @return Staff's information
     */
    Staff findStaffByStaffId(Long staffId);

    /**
     * This method allows to find staff by username of user
     *
     * @param username username of user
     * @return Staff's information
     */
    Staff findStaffByUsername(String username);

    /**
     * This method allows to find staff by user id
     *
     * @param userId identity of user
     * @return Staff's information
     */
    Staff findStaffByUserId(Long userId);

    /**
     * This method allows to search staffs by keyword
     *
     * @param keyword name of staff
     * @param page    page of list
     * @param size    size of list
     * @return Staffs list
     */
    PaginationWrapper<List<Staff>> searchStaffs(String keyword, Pageable pageable);

    /**
     * This method allows to lock or unlock user
     * 
     * @param staffId Identity of staff
     */
    void LockAndUnlockStaff(Long staffId);

    /**
     * This method allows to update a new user
     * 
     * @param staffId Identity of staff
     */
    void deleteStaff(Long staffId);
}
