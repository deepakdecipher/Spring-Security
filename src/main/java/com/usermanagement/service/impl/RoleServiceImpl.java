package com.usermanagement.service.impl;

import com.usermanagement.modelentity.Role;
import com.usermanagement.modelentity.User;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public Role save(Role role) {
        return roleRepository.findByRoleName(role.getRoleName()).orElseGet(() -> roleRepository.save(role));
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role findById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    public Role findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName).orElse(null);
    }

    @Override
    public String updateRole(Long id, Role role) {
        Optional<Role> checkRoleId = roleRepository.findById(id);

        if (checkRoleId.isEmpty()) {
            return "Id not present";
        }

        if ("ADMIN".equals(checkRoleId.get().getRoleName())) {
            return "Can not modify 'ADMIN' role name";
        }

        roleRepository.findByRoleName(role.getRoleName())
                .orElseThrow(RuntimeException::new);

        Role oldRole = checkRoleId.get();
        oldRole.setId(id);

        checkRoleId.get().setRoleName(role.getRoleName());
        roleRepository.save(checkRoleId.get());

        updateUsersRoles(oldRole, checkRoleId.get());

        return "Details changed successfully";
    }

    private void updateUsersRoles(Role oldRole, Role newRole) {
        List<User> users = userRepository.findAll();

        users.forEach(user -> {
            Set<Role> roles = user.getRoles();
            if (roles.contains(oldRole)) {
                roles.remove(oldRole);
                if (newRole != null) {
                    roles.add(newRole);
                }
                userRepository.save(user);
            }
        });
    }

    @Override
    public String deleteRoleById(Long id) {
        Optional<Role> role = roleRepository.findById(id);

        if (role.isPresent()) {
            updateUsersRoles(role.get(), null);
            roleRepository.deleteById(id);
            return "Role deleted with id: " + id;
        }

        return "Id not found";
    }
}
