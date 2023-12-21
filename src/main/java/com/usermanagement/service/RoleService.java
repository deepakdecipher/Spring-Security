package com.usermanagement.service;

import com.usermanagement.modelentity.Role;

import java.util.List;

public interface RoleService {


    Role save(Role role);

    List<Role> findAll();

    Role findById(Long id);

    Role findByRoleName(String roleName);

    String updateRole(Long id, Role role);

    String deleteRoleById(Long id);

}
