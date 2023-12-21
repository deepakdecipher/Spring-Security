/*
package com.usermanagement.service;

import com.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Autowired
    UserServiceImpl userServiceImpl;

    @BeforeEach
    void setUp() {
        userServiceImpl = new UserServiceImpl(userRepository);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void save() {
    }

    @Test
    void findAll() {
        userServiceImpl.listAll();
        verify(userRepository).findAll();

    }

    @Test
    void findUsersExceptLIU() {
    }

    @Test
    void findLIU() {
    }

    @Test
    void findById() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void findByFullName() {
    }

    @Test
    void findByUserName() {
    }

    @Test
    void deleteUserById() {
    }

    @Test
    void resetPassword() {
    }

    @Test
    void addRoleToUser() {
    }

}*/
