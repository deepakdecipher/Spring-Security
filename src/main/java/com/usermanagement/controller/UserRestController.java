package com.usermanagement.controller;

import com.usermanagement.jwt.JwtService;
import com.usermanagement.modelrequest.*;
import com.usermanagement.modelresponse.JwtResponse;
import com.usermanagement.modelresponse.UserResponse;
import com.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;

/**
 * Default user REST controller.
 *
 * <p><b>Configuring the base path:</b> set {@code user-management.api.user-base-path}
 * in your {@code application.yaml} (default: {@code /userApi}).
 *
 * <p><b>Overriding individual endpoints:</b> create your own controller class
 * annotated with {@code @RestController} and set it as the missing bean:
 * <pre>
 * {@literal @}Bean
 * {@literal @}Primary
 * public UserRestController myController(UserService svc, JwtService jwt) {
 *     return new MyCustomUserController(svc, jwt);
 * }
 * </pre>
 * Or simply extend this class and override the methods you need.
 *
 * <p>Backed off when the consuming app provides its own {@code UserRestController} bean.
 */
@RestController
@RequestMapping("${user-management.api.user-base-path:/userApi}")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final JwtService jwtService;

    // ── Public ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest request) {
        return ResponseEntity.ok(jwtService.generateToken(request));
    }

    @PostMapping("/login-otp")
    public ResponseEntity<JwtResponse> loginWithOtp(@Valid @RequestBody LoginOtpRequest request) {
        return ResponseEntity.ok(jwtService.generateTokenFromOtp(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(jwtService.refreshToken(refreshToken));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody UserSignUp request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(request));
    }

    @PostMapping("/verify-otp/{otp}")
    public ResponseEntity<String> verifyOtp(
            @PathVariable String otp,
            @RequestParam String emailId) {
        return ResponseEntity.ok(userService.verifyOtp(otp, emailId));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPassword request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }

    // ── Authenticated ─────────────────────────────────────────────────────────

    @PostMapping("/generate-otp/{emailId}")
    public ResponseEntity<String> generateOtp(@PathVariable String emailId) {
        return ResponseEntity.ok(userService.generateOtp(emailId));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ── Admin only ─────────────────────────────────────────────────────────────

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listAll() {
        return ResponseEntity.ok(userService.listAll());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.findLIU());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<List<UserResponse>> listExceptSelf() {
        return ResponseEntity.ok(userService.findUsersExceptLIU());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/fullname/{fullName}")
    public ResponseEntity<List<UserResponse>> findByFullName(@PathVariable String fullName) {
        return ResponseEntity.ok(userService.findByFullName(fullName));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/username/{userName}")
    public ResponseEntity<UserResponse> findByUserName(@PathVariable String userName) {
        return ResponseEntity.ok(userService.findByUserName(userName));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdate request) {
        return ResponseEntity.ok(userService.updateUserDetails(id, request));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/add-roles")
    public ResponseEntity<String> addRoles(@Valid @RequestBody AssignRolesToUser request) {
        return ResponseEntity.ok(userService.addRoleToUser(request));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUserById(id));
    }
}
