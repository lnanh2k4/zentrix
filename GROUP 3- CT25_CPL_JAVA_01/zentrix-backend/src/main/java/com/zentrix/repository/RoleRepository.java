package com.zentrix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.Role;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  February 18, 2025
 */

public interface RoleRepository extends JpaRepository<Role, Integer> {
    /**
     * This method allows to find Role by name of role
     * 
     * @param roleName name of role
     * @return information of role
     */
    Role findByRoleName(String roleName);

}
