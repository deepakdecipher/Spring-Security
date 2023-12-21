/*
package com.usermanagement.util;

import com.usermanagement.modelentity.Role;
import com.usermanagement.modelentity.User;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class BootstrapAppData {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @EventListener
    public void insertData(ContextRefreshedEvent event) {


        Optional<Role> tempRoleAdmin = roleRepository.findByRoleName("ADMIN");
        if (!tempRoleAdmin.isPresent()) {
            Role roleAdmin = new Role();
            roleAdmin.setRoleName("ADMIN");
            roleRepository.save(roleAdmin);
        }
        Optional<User> user = userRepository.findByUserName("admin");
        if (!user.isPresent()) {
            User userAdmin = new User();
            userAdmin.setUserFullName("ADMINISTRATOR");
            userAdmin.setEmail("admin");
            userAdmin.setPassword(passwordEncoder.encode("admin"));
            userAdmin.setEmail("administrator@company.com");

            tempRoleAdmin = roleRepository.findByRoleName("ADMIN");
            Set<Role> roles = new HashSet<>();
            roles.add(tempRoleAdmin.get());
            userAdmin.setRoles(roles);
            userRepository.save(userAdmin);

        }

    }

}
*/
