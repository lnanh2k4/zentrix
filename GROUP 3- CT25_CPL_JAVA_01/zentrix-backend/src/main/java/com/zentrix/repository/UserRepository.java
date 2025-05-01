package com.zentrix.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zentrix.model.entity.User;
import java.util.Optional;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * This method allows to find user by username
     * 
     * @param username username of user
     * @return User information
     */
    User findUserByUsername(String username);

    /**
     * This method allows to search users by keyword
     * 
     * @param keyword      keyword to search user
     * @param pageable     pageable of list
     * @param status       status of user that does not take
     * @param customerRole role of customer
     * @return user with pagination
     */
    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE CONCAT('%', LOWER(:keyword), '%') OR LOWER(u.lastName) LIKE CONCAT('%', LOWER(:keyword), '%')) AND u.status != :status AND LOWER(u.roleId.roleName) = LOWER(:customerRole) ORDER BY u.userId DESC")
    Page<User> searchUser(@Param("keyword") String keyword, Pageable pageable, @Param("status") int status,
            @Param("customerRole") String customerRole);

    /**
     * This method allows to get all customets
     * 
     * @param pageable     pageable of list
     * @param status       status of user that does not take
     * @param customerRole role of customer
     * @return user with pagination
     */
    @Query("SELECT u FROM User u WHERE u.status != :status AND u.roleId.roleName = :customerRole ORDER BY u.userId DESC")
    Page<User> findAll(Pageable pageable, @Param("status") Integer status, @Param("customerRole") String customerRole);

    /**
     * This method allows to find user by phone
     * 
     * @param phone phone number of user
     * @return list of user
     */
    User findByPhone(String phone);

    /**
     * This method allows to find user by email
     * 
     * @param email email of user
     * @return optional of user
     */
    Optional<User> findByEmail(String email);
}
