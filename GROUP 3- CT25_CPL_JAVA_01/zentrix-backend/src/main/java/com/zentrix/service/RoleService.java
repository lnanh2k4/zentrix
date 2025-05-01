package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Role;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  February 18, 2025
 */

public interface RoleService {

    /**
     * This method allows to create a new role
     * 
     * @param roleName name of role
     * @return information of role
     */
    Role createRole(String roleName);

    /**
     * This method allows to delete role by identity of role
     * 
     * @param roleId identity of role
     */
    void deleteRole(Integer roleId);

    /**
     * This method allows to delete role by name of role
     * 
     * @param roleName name of role
     */
    void deleteRoleByName(String roleName);

    /**
     * This method allows to find role by identity of role
     * 
     * @param roleId identity of role
     * @return information of role
     */
    Role findRoleById(Integer roleId);

    /**
     * This method allows to find role by name of role
     * 
     * @param roleName name of role
     * @return information of role
     */
    Role findRoleByName(String roleName);

    /**
     * This method allows to get roles list
     * 
     * @return roles list
     */
    PaginationWrapper<List<Role>> getAllRoles(int page, int size);

    /**
     * This method allows to get All roles
     * 
     * @return list of roles
     */
    List<Role> getAllRoles();

}
