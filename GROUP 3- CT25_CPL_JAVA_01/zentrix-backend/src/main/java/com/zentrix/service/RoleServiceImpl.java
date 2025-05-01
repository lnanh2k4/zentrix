package com.zentrix.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.zentrix.model.entity.Role;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.RoleRepository;

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
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = ActionFailedException.class)
    @Override
    public Role createRole(String name) {
        return roleRepository.save(Role.builder().roleName(name).build());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = ActionFailedException.class)
    @Override
    public void deleteRole(Integer roleId) {
        roleRepository.delete(roleRepository.findById(roleId)
                .orElseThrow(() -> new ValidationFailedException(AppCode.ROLE_NOT_FOUND)));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = ActionFailedException.class)
    @Override
    public void deleteRoleByName(String roleName) {
        roleRepository.delete(findRoleByName(roleName));
    }

    @Override
    public Role findRoleById(Integer roleId) {
        return roleRepository.findById(roleId).get();
    }

    @Override
    public Role findRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    @Override
    public PaginationWrapper<List<Role>> getAllRoles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var rolePage = roleRepository.findAll(pageable);
        return new PaginationWrapper.Builder<List<Role>>()
                .setData(rolePage.getContent())
                .setPaginationInfo(rolePage)
                .build();
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

}
