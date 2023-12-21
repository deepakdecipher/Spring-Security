package com.usermanagement.controller;

import com.usermanagement.jwt.JwtService;
import com.usermanagement.modelrequest.*;
import com.usermanagement.modelresponse.JwtResponse;
import com.usermanagement.modelresponse.UserResponse;
import com.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/userApi")
@AllArgsConstructor
public class UserRestController {

    UserService userService;
    JwtService jwtService;

    //LOGIN
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest jwtRequest) {
        return new ResponseEntity<>(jwtService.generateToken(jwtRequest), HttpStatus.OK);
    }
    // SIGN-UP
    @PreAuthorize("permitAll()")
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody UserSignUp userSignUp) {
        return new ResponseEntity<>(userService.save(userSignUp), HttpStatus.OK);
    }
    // UPDATE USER
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UserUpdate userUpdate) {
        return new ResponseEntity<>(userService.updateUserDetails(id, userUpdate), HttpStatus.OK);
    }
    // LIST ALL USERS
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listAllUsers() {
        return new ResponseEntity<>(userService.listAll(), HttpStatus.OK);
    }

    // FETCH LOGGED IN USERS
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> fetchLIU() {
        return new ResponseEntity<>(userService.findLIU(), HttpStatus.OK);
    }

    // FETCH USERS EXCEPT LOGGED IN USER
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<List<UserResponse>> fetchAllUsersExceptLIU() {
        return new ResponseEntity<>(userService.findUsersExceptLIU(), HttpStatus.OK);
    }
    // FETCH USER BY FULL NAME
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/fullname/{fullName}")
    public ResponseEntity<List<UserResponse>> findUserByFullName(@PathVariable("fullName") String fullName) {
        return new ResponseEntity<>(userService.findByFullName(fullName), HttpStatus.OK);
    }
    // FETCH USER BY USER NAME
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/username/{email}")
    public ResponseEntity<UserResponse> findUserByUserName(@PathVariable("email") String userName) {
        return new ResponseEntity<>(userService.findByUserName(userName),HttpStatus.OK);
    }
    // FIND USER BY ID
    @GetMapping("/id/{id}")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<UserResponse> findUserById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(userService.findById(id), HttpStatus.OK);
    }
    // RESET PASSWORD
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(value = "/reset-password",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPassword resetPassword) {
        return new ResponseEntity<>(userService.resetPassword(resetPassword), HttpStatus.OK);
    }

    // ASSIGN ROLE TO USERS
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(value = "/add-roles",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addRolesToUsers(@Valid @RequestBody AssignRolesToUser assignRolesToUser) {
        return new ResponseEntity<>(userService.addRoleToUser(assignRolesToUser),HttpStatus.OK);
    }
    // DELETE USER BY ID
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<UserResponse> deleteUserById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(userService.deleteUserById(id), HttpStatus.OK);
    }
    //GENERATE OTP
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    @PostMapping(value = "/generate-otp/{emailId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateOtp(@PathVariable("emailId") String emailId) {
        return new ResponseEntity<>(userService.generateOtp(emailId), HttpStatus.OK);
    }
    //VERIFY OTP
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    @PostMapping(value = "/verify-otp/{otp}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyOtp(@PathVariable("otp") String otp, @RequestParam("emailId") String emailId) {
        return new ResponseEntity<>(userService.verifyOtp(otp, emailId), HttpStatus.OK);
    }
}
