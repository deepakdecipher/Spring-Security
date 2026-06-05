package com.usermanagement.controller;

import com.usermanagement.modelentity.Role;
import com.usermanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Default role management controller.
 *
 * <p>Base path is configurable via {@code user-management.api.role-base-path}
 * (default: {@code /roleApi}). Override this bean or extend this class to
 * customise role management behaviour.
 */
@RestController
@RequestMapping("${user-management.api.role-base-path:/roleApi}")
@RequiredArgsConstructor
public class RoleRestController {

    private final RoleService roleService;

    @PostMapping("/add-role")
    public Role addRole(@RequestBody Role role) {
        return roleService.save(role);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/update/{id}")
    public String updateRole(@PathVariable Long id, @RequestBody Role role) {
        return roleService.updateRole(id, role);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/roles")
    public List<Role> listRoles() {
        return roleService.findAll();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public String deleteRole(@PathVariable Long id) {
        return roleService.deleteRoleById(id);
    }
}
