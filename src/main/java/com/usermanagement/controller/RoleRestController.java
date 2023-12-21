package com.usermanagement.controller;

import com.usermanagement.modelentity.Role;
import com.usermanagement.service.impl.RoleServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roleApi")
@AllArgsConstructor
public class RoleRestController {

    RoleServiceImpl roleServiceImpl;
    // ADD ROLE
    @PostMapping("/add-role")
    public Role addNewRole(@RequestBody Role role) {
        return roleServiceImpl.save(role);
    }
    // UPDATE ROLE
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/update/{id}")
    public String updateRole(@PathVariable("id") Long id, @RequestBody Role role) {
        return roleServiceImpl.updateRole(id, role);
    }
    // LIST ALL ROLES
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/roles")
    public List<Role> listAllRoles() {
        return roleServiceImpl.findAll();
    }
    // DELETE ROLE BY ID
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public String deleteRole(@PathVariable("id") Long id) {
        return roleServiceImpl.deleteRoleById(id);
    }

}
