/*
package com.usermanagement.repository;

import com.usermanagement.modelentity.Role;
import com.usermanagement.modelentity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void findByUserFullName() {
        User user = getUser();
        userRepository.save(user);
        Optional<List<User>> result = userRepository.findByUserFullName(user.getUserFullName());
        Assertions.assertEquals(user,result.get().get(0));
    }

    @Test
    void findByUserName() {
        User user = getUser();
        userRepository.save(user);
        User result = userRepository.findByUserName(user.getUserName()).get();
        Assertions.assertEquals(user,result);
    }

    private User getUser(){
        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().id(2L).roleName("USER").build());
        return User.builder().id(2L).userFullName("Fake Name").email("fakemail@company.com")
                .userName("username1").password(passwordEncoder.encode("password1"))
                .roles(roles).build();
    }
}*/
