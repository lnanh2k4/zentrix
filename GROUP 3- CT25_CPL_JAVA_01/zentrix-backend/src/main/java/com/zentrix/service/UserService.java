package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.User;
import com.zentrix.model.response.PaginationWrapper;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/

public interface UserService {

    /**
     * This method allows to find user by user id
     * 
     * @param userId Identity of user
     * @return User object
     */
    User findUserByUserId(Long userId);

    /**
     * This method allows to update a new user
     * 
     * @param userDetails information of user
     * @return User information
     */
    User updateUser(User userDetails);

    /**
     * This method allows to create a new user
     * 
     * @param user object
     * @return User information
     */
    User createUser(User user);

    /**
     * This method allows to lock or unlock user
     * 
     * @param userId Identity of user
     */
    void LockAndUnlockUser(Long userId);

    /**
     * This method allows to find user by username
     * 
     * @param username username of user
     * @return User information
     */
    User findUserByUsername(String username);

    /**
     * This method allows to get users list
     * 
     * @param page page of list
     * @param size size of list
     * @return users list
     */
    PaginationWrapper<List<User>> getAllUsers(int page, int size);

    /**
     * This method allows to search users
     * 
     * @param keyword name of user
     * @param page    page of list
     * @param size    size of list
     * @return users list
     */
    PaginationWrapper<List<User>> searchUser(String keyword, int page, int size);

    /**
     * This method allows to update a new user
     * 
     * @param userId Identity of user
     */
    void deleteUser(Long userId);

    /**
     * This method allows to find user by phone
     * 
     * @param phone phone number
     * @return list of user
     */
    User findUsersByPhone(String phone);

    /**
     * This method allows to find user by email
     * 
     * @param email email of user
     * @return information of user
     */
    User findUserByEmail(String email);

    public void validateCustomerUniqueContract(String username, String email, String phone);

    public void validateCustomerUniqueUsername(String username);

    public void validateCustomerUniqueEmail(String email);

    public void validateCustomerUniquePhone(String phone);

    public void verifyRequest(String identifier, String method, String type);

    public void resetPasswordWithToken(String token, String newPassword);

    public void resetPasswordWithOtp(String otp, String newPassword);

    public boolean verifyWithToken(String token);

}
