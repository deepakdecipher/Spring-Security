package com.usermanagement.service.impl;

import com.usermanagement.config.UserManagementProperties;
import com.usermanagement.emailservice.EmailService;
import com.usermanagement.exception.*;
import com.usermanagement.modelentity.Role;
import com.usermanagement.modelentity.User;
import com.usermanagement.modelrequest.AssignRolesToUser;
import com.usermanagement.modelrequest.ResetPassword;
import com.usermanagement.modelrequest.UserSignUp;
import com.usermanagement.modelrequest.UserUpdate;
import com.usermanagement.modelresponse.UserResponse;
import com.usermanagement.otp.GenerateOtp;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final GenerateOtp generateOtp;
    private final UserManagementProperties properties;

    // ── Registration ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse save(UserSignUp req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered.");
        }
        if (userRepository.existsByUserName(req.getUserName())) {
            throw new UserAlreadyExistsException("Username already taken.");
        }

        Set<Role> roles = req.getRole().stream()
                .map(r -> Role.builder().roleName(r.getRoleName()).build())
                .collect(Collectors.toSet());

        User user = User.builder()
                .userFullName(req.getUserFullName())
                .userName(req.getUserName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .verified(false)
                .roles(roles)
                .build();

        userRepository.save(user);
        sendOtpEmail(req.getEmail());
        return toResponse(user);
    }

    // ── OTP ───────────────────────────────────────────────────────────────────

    @Override
    public String generateOtp(String email) {
        userRepository.findByEmailIncludingUnverified(email)
                .orElseThrow(() -> new EmailNotFoundException("Email not registered."));
        sendOtpEmail(email);
        return "OTP sent to " + email;
    }

    @Override
    @Transactional
    public String verifyOtp(String otp, String email) {
        generateOtp.validateOtp(otp, email);
        User user = userRepository.findByEmailIncludingUnverified(email)
                .orElseThrow(() -> new EmailNotFoundException("User not found."));
        user.setVerified(true);
        userRepository.save(user);
        return "Email verified successfully. You can now log in.";
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public List<UserResponse> listAll() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) throw new UsernameNotFoundException("No users found.");
        return users.stream().map(this::toResponse).toList();
    }

    @Override
    public List<UserResponse> findUsersExceptLIU() {
        String loggedIn = currentEmail();
        return userRepository.findAll().stream()
                .filter(u -> !loggedIn.equals(u.getEmail()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse findLIU() {
        return userRepository.findByEmail(currentEmail())
                .map(this::toResponse)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found."));
    }

    @Override
    public UserResponse findById(long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new UserIdNotFoundException("User not found with id: " + id));
    }

    @Override
    public List<UserResponse> findByFullName(String fullName) {
        return userRepository.findByUserFullName(fullName)
                .map(list -> list.stream().map(this::toResponse).toList())
                .orElse(List.of());
    }

    @Override
    public UserResponse findByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .map(this::toResponse)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + userName));
    }

    // ── Updates ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse updateUserDetails(Long id, UserUpdate req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException("User not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (!isAdmin && !auth.getName().equals(user.getEmail())) {
            throw new UnAuthorisedException("Not authorised to update this user.");
        }

        user.setUserFullName(req.getUserFullName());
        user.setUserName(req.getUserName());
        user.setEmail(req.getEmail());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public String resetPassword(ResetPassword req) {
        User user = userRepository.findByUserName(req.getUserName())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found."));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new WrongPasswordException("Old password does not match.");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return "Password changed successfully.";
    }

    @Override
    @Transactional
    public String addRoleToUser(AssignRolesToUser req) {
        User user = userRepository.findByUserName(req.getUserName())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found."));

        req.getRoles().forEach(r ->
                user.getRoles().add(Role.builder().roleName(r.getRoleName()).build()));
        userRepository.save(user);
        return "Roles added successfully.";
    }

    @Override
    @Transactional
    public UserResponse deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException("User not found with id: " + id));
        UserResponse response = toResponse(user);
        userRepository.deleteById(id);
        return response;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void sendOtpEmail(String email) {
        String otp = generateOtp.generateOtp(email);
        String body = buildOtpBody(otp);
        emailService.sendInternalServerErrorEmailNotification(
                email,
                properties.getEmail().getFrom(),
                properties.getEmail().getSubject(),
                body,
                null
        );
    }

    private String buildOtpBody(String otp) {
        return """
                <html>
                <body style="font-family:Arial,sans-serif;padding:20px;">
                  <h2>OTP Verification</h2>
                  <p>Your one-time password:</p>
                  <h1 style="letter-spacing:8px;color:#e74c3c;">%s</h1>
                  <p>Valid for <strong>%d minute(s)</strong>.</p>
                </body>
                </html>
                """.formatted(otp, properties.getOtp().getExpirationMinutes());
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userFullName(user.getUserFullName())
                .userName(user.getUserName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
}
