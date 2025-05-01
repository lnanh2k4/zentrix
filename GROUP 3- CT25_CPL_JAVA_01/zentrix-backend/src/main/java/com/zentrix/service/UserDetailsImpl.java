package com.zentrix.service;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.zentrix.model.entity.User;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 18, 2025
*/

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDetailsImpl implements UserDetailsService {
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // find USER by username
        User user = userRepository.findUserByUsername(username);
        // check condition of user. If user is null, it will throw exception
        if (user == null)
            throw new ValidationFailedException(AppCode.USER_NOT_FOUND);
        // return new user with username, password and authorities
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                getAuthorities(user));
    }

    /**
     * This method allows to get authorities
     * 
     * @param user information of user
     * @return collection that can extends Granted Authority
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        String roleName = user.getRoleId().getRoleName().toUpperCase();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

}
