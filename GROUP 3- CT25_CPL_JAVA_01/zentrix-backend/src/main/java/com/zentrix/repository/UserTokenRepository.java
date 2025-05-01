package com.zentrix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.User;
import com.zentrix.model.entity.UserToken;
import java.util.Optional;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  April 10, 2025
 */
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    /**
     * This method allows to find user token by token
     * 
     * @param token token
     * @return user token's information
     */
    Optional<UserToken> findByToken(String token);

    /**
     * This method allows to find user token by one-time password
     * 
     * @param otp one-time password
     * @return information of user token
     */
    Optional<UserToken> findByOtp(String otp);

    /**
     * This method allows to find user token by user id
     * 
     * @param userId identity of user
     * @return user token
     */
    Optional<UserToken> findByUserId(User userId);
}
