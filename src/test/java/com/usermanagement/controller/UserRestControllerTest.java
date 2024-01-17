/*
package com.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.usermanagement.config.SecurityConfig;
import com.usermanagement.jwt.JwtAuthenticationFilter;
import com.usermanagement.jwt.JwtServiceImpl;
import com.usermanagement.jwt.JwtUtil;
import com.usermanagement.modelentity.Role;

import com.usermanagement.modelrequest.JwtRequest;
import com.usermanagement.modelrequest.UserSignUp;
import com.usermanagement.modelresponse.JwtResponse;
import com.usermanagement.modelresponse.UserResponse;
import com.usermanagement.service.CustomUserDetailsService;
import com.usermanagement.service.impl.UserServiceImpl;
//import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(value = UserRestController.class,
        includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtUtil.class, SecurityConfig.class})}
)
class UserRestControllerTest {

    @MockBean
    private UserServiceImpl userServiceImpl;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    @MockBean
    private JwtServiceImpl jwtServiceImpl;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    JwtUtil jwtUtil;
    @MockBean
    JwtRequest jwtRequest;
    @MockBean
    JwtResponse jwtResponse;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    ObjectMapper objectMapper;

    private UserResponse userResponse;
    private UserSignUp userSignUp;
    private List<UserResponse> userList;
    private String jwtToken;
    @BeforeEach
    void setUp() throws Exception {
        userResponse = getUserResponse();
        userSignUp = getUserSignUp();
        userList = getUserList();
        jwtRequest = getJwtRequest();
        List<GrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ADMIN");
        authorities.add(authority);
        UserDetails dummy = new User("admin", "admin", authorities);
        jwtToken = jwtUtil.generateToken(dummy);
        given(userServiceImpl.listAll()).willReturn(getUserList());
        when(customUserDetailsService.loadUserByUsername(eq("admin"))).thenReturn(dummy);
    }

    @Test
    void saveUser() throws Exception {

        when(userServiceImpl.save(any(UserSignUp.class))).thenReturn(userResponse);

        ResultActions response = mockMvc.perform(post("/userApi/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userSignUp)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userResponse.getId()))
                .andExpect(jsonPath("$.userFullName").value(userResponse.getUserFullName()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()))
                .andExpect(jsonPath("$.email").value(userResponse.getUserName()))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void listAllUsers() throws Exception {
        ResultActions response = mockMvc.perform(get("/userApi/users", getUserList())
                        .header("Authorization", "Bearer "+jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(userList.size()))
                .andDo(print());
    }

    @Test
    void fetchLIU() throws Exception{
        given(userServiceImpl.findLIU()).willReturn(userResponse);
        ResultActions response = mockMvc.perform(get("/userApi/me", userResponse)
                        .header("Authorization", "Bearer "+jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userResponse.getId()))
                .andDo(print());
    }

    @Test
    void fetchAllUsersExceptLIU() throws Exception{
        given(userServiceImpl.findUsersExceptLIU()).willReturn(getUserList());
        ResultActions response = mockMvc.perform(get("/userApi/list", userList)
                        .header("Authorization", "Bearer "+jwtToken))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()").value(userList.size()));
    }

    @Test
    void findUserById() {
    }

    @Test
    void generateToken() throws Exception{
//        given(jwtServiceImpl.generateToken(any())).willReturn(jwtToken);
//        ResultActions response = mockMvc.perform(post("/userApi/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(jwtRequest)))
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andExpect(jsonPath("$.token").value(jwtUtil.generateToken()));
    }

    @Test
    void updateUser() {
    }

    @Test
    void resetPassword() {
    }


    @Test
    void deleteUserById() {
    }

    private UserResponse getUserResponse() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().id(1).roleName("USER").build());
        return UserResponse.builder().id(1).userFullName("Fake Name").email("fakeemail@company.com")
                .userName("username1").roles(roles).build();
    }

    private List<UserResponse> getUserList() {
        List<UserResponse> users = new ArrayList<>();
        users.add(getUserResponse());
        return users.stream().map(u -> UserResponse.builder()
                .id(u.getId()).userFullName(u.getUserFullName()).email(u.getEmail())
                .userName(u.getUserName()).roles(u.getRoles()).build()).toList();
    }

    private UserSignUp getUserSignUp() {
        return UserSignUp.builder()
                .userFullName("Fake Name").email("fakeemail@company.com")
                .userName("username1").password("password1")
                .build();
    }

    private JwtRequest getJwtRequest() {
            return JwtRequest.builder().email("admin").password("admin").build();
    }

}*/
