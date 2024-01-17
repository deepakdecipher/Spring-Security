package com.usermanagement.service.impl;

import com.usermanagement.emailconfig.EmailNotificationProperties;
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
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import lombok.AllArgsConstructor;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.json.JSONObject;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.usermanagement.emailconfig.EmailConstant.*;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private RoleServiceImpl roleServiceImpl;
    private PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;
    private EmailNotificationProperties emailNotificationProperties;
    private EmailService emailService;
    private VelocityEngine velocityEngine;
    private GenerateOtp generateOtp;

    @Override
    public UserResponse save(UserSignUp userSignUp) {
        prepareOtp(userSignUp.getEmail());
        Optional<User> existingUser = userRepository.findByEmail(userSignUp.getEmail());
        Optional<User> existingUserName = userRepository.findByUserName(userSignUp.getUserName());

        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User email already exists.");
        }
        if (existingUserName.isPresent()) {
            throw new UserAlreadyExistsException("User name already exists.");
        } else {
            User newUser = saveUser(userSignUp);
            return userToUserResponse(newUser);
        }
    }
    @Override
    public List<UserResponse> listAll() {
        return userToUserResponse(userRepository.findAll());
    }

    @Override
    public List<UserResponse> findUsersExceptLIU() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = authentication.getName();
        List<User> allUsers = userRepository.findAll();
        return userToUserResponse(allUsers.stream()
                .filter(user -> !loggedInEmail.contains(user.getEmail()))
                .toList());
    }


    @Override
    public UserResponse findLIU() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return (userToUserResponse(userRepository.findByEmail(email).get()));
    }

    @Override
    public UserResponse findById(long id) {
        return (userToUserResponse(userRepository.findById(id).get()));
    }

    @Override
    public UserResponse updateUserDetails(Long id, UserUpdate userUpdate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return updateUser(id, userUpdate, authentication);
    }
    private UserResponse updateDetails(Long id, UserUpdate userUpdate) {
        AtomicReference<UserResponse> userResponse = new AtomicReference<>(new UserResponse());
        Optional<User> user = userRepository.findByUserName(userUpdate.getUserName());
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Username not found.");
        }
        user.ifPresentOrElse(userObj -> {
            if (userObj.getId().equals(id)) {
                userObj.setUserName(userUpdate.getUserName());
                userObj.setUserFullName(userUpdate.getUserFullName());
                userObj.setEmail(userUpdate.getEmail());
                userRepository.save(userObj);
                userResponse.set(userToUserResponse(userObj));
            } else {
                throw new UserIdNotMatchedException("User id not matched with the logged in user.");
            }
        }, () -> {
            throw new UserAlreadyExistsException("Invalid user id");
        });
        return userResponse.get();
    }

    @Override
    public List<UserResponse> findByFullName(String fullName) {
        Optional<List<User>> user = userRepository.findByUserFullName(fullName);
        return user.map(this::userToUserResponse).orElse(null);
    }

    @Override
    public UserResponse findByUserName(String userName) {
        Optional<User> user = userRepository.findByUserName(userName);
        if(user.isPresent()) {
            return user.map(this::userToUserResponse).orElse(null);
        }else {
            throw new UsernameNotFoundException("Username not found.");
        }
    }

    @Override
    public UserResponse deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return deleteUserResponse(userRepository.findById(id).get());
        } else {
            throw new UsernameNotFoundException("User not found.");
        }

    }

    private UserResponse deleteUserResponse(User user) {
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("No logged in users found in the system.");
        } else {
            return UserResponse.builder()
                    .id(user.getId()).userFullName(user.getUserFullName()).email(user.getEmail())
                    .userName(user.getUserName()).roles(user.getRoles()).build();
        }
    }

    @Override
    public String addRoleToUser(AssignRolesToUser assignRolesToUser) {
        Optional<User> user = userRepository.findByUserName(assignRolesToUser.getUserName());
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found.");
        }
        Set<Role> roles = user.get().getRoles();
        roles.addAll(assignRolesToUser.getRoles());
        roles.forEach(role -> role.setUser(user.get()));
        userRepository.save(user.get());
        return JSONObject.quote("Roles added successfully");
    }

    @Override
    public String generateOtp(String emailId) {
        AtomicReference<String> otpResponse = new AtomicReference<>();
        userRepository.findByEmail(emailId).ifPresentOrElse(user -> {
            otpResponse.set(prepareOtp(emailId));
        },()-> {
            throw new EmailNotFoundException("Email not found. Please enter the correct email id.");
        });
       return otpResponse.get();
    }

    @Override
    public String verifyOtp(String otp, String emailId) {
        generateOtp.validateOtp(otp, emailId);
        return null;
    }

    @Override
    public String resetPassword(ResetPassword resetPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (resetPassword.getUserName().equals(authentication.getName())) {
            return changePassword(resetPassword);
        } else if (userRepository.findByEmail(authentication.getName()).get().getRoles().stream()
                .map(Role::getRoleName).toList().contains("ADMIN")) {
            return changePassword(resetPassword);
        } else {
            throw new UnAuthorisedException("You are not authorised to change the password.");
        }

    }

    private String changePassword(ResetPassword resetPassword) {
        Optional<User> user = userRepository.findByUserName(resetPassword.getUserName());
        if (user.isPresent()) {
            if (passwordEncoder.matches(resetPassword.getOldPassword(), user.get().password)) {
                user.get().setPassword(passwordEncoder.encode(resetPassword.getNewPassword()));
                userRepository.save(user.get());
                return JSONObject.quote("Password changed successfully.");
            } else {
                throw new WrongPasswordException("Old password does not match. Please enter correct old password.");
            }
        }
        throw new UsernameNotFoundException("User name not found.");
    }

    private UserResponse userToUserResponse(User user) {
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("No logged in users found in the system.");
        } else {
            return UserResponse.builder()
                    .id(user.getId()).userFullName(user.getUserFullName()).email(user.getEmail())
                    .userName(user.getUserName()).roles(user.getRoles()).build();
        }
    }

    private List<UserResponse> userToUserResponse(List<User> user) {
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("No users found in the system.");
        } else {
            return user.stream().map(u -> UserResponse.builder()
                    .id(u.getId()).userFullName(u.getUserFullName()).email(u.getEmail())
                    .userName(u.getUserName()).roles(u.getRoles()).build()).toList();
        }
    }
    private User saveUser(UserSignUp userSignUp) {
        User newUser = User.builder()
                .userFullName(userSignUp.getUserFullName())
                .email(userSignUp.getEmail())
                .userName(userSignUp.getUserName())
                .password(passwordEncoder.encode(userSignUp.getPassword()))
                .build();
        Set<Role> roles = userSignUp.getRole().stream()
                .map(role -> {
                    Role newRole = new Role();
                    newRole.setRoleName(role.getRoleName());
                    newRole.setUser(newUser);
                    return newRole;
                })
                .collect(Collectors.toSet());

        newUser.setRoles(roles);
        userRepository.save(newUser);
        return newUser;
    }
    private UserResponse updateUser(Long id, UserUpdate userUpdate, Authentication authentication) {
        Optional<User> tempUser = userRepository.findById(id);
        if (tempUser.isPresent()) {
            if (userRepository.findByEmail(authentication.getName()).get().getRoles().stream()
                    .map(Role::getRoleName).toList().contains("ADMIN")) {
                return updateDetails(id, userUpdate);
            } else if (tempUser.get().getUserName().equals(authentication.getName())) {
                return updateDetails(id, userUpdate);
            } else {
                throw new UnAuthorisedException("You are not authorize to change the user details");
            }
        } else {
            throw new UserIdNotFoundException("User ID not present");
        }
    }
    public String prepareOtp(String emailId) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(emailNotificationProperties.getSmtpHost());
        javaMailSender.setPort(emailNotificationProperties.getSmtpPort());
        javaMailSender.setUsername(emailNotificationProperties.getSmtpUsername());
        javaMailSender.setPassword(emailNotificationProperties.getSmtpPassword());

        Properties props = new Properties();
        props.put(MAIL_SMTP_HOST, emailNotificationProperties.getSmtpHost());
        props.put(MAIL_SMTP_PORT, emailNotificationProperties.getSmtpPort());
        props.put(MAIL_TRANSPORT_PROTOCOL, "smtp");
        props.put(MAIL_SMTP_AUTH, "true");
        props.put(MAIL_SMTP_SSL_ENABLE, "false");
        props.put(MAIL_SMTP_STARTTLS_ENABLE, "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailNotificationProperties.getSmtpUsername(), emailNotificationProperties.getSmtpPassword());
            }
        });
        javaMailSender.setSession(session);
        String content = prepareEmailBody(emailId);
        emailNotificationProperties.setSmtpEmailSubject("OTP Verification "+new Date());
        emailService.sendInternalServerErrorEmailNotification(
                emailId, emailNotificationProperties.getSmtpEmailFrom(),
                emailNotificationProperties.getSmtpEmailSubject(), content, javaMailSender);
        return JSONObject.quote("Otp sent to mail. Please check and verify the otp");
    }

    private String prepareEmailBody(String emailId) {
        StringWriter stringWriter = new StringWriter();
        VelocityContext velocityContext = new VelocityContext();
        JSONObject jsonObject = generateOtp.generateOtp(emailId);
        velocityContext.put("message", jsonObject.get("message"));
        velocityContext.put("otp", jsonObject.get("One Time Password"));
        String utf8 = "UTF-8";
        velocityEngine.mergeTemplate("velocity/opt-generation.vm", utf8, velocityContext, stringWriter);
        return stringWriter.toString();
    }
    public static String generateOtp(int otpLength) {
        String otpChars = "0123456789";
        StringBuilder otp = new StringBuilder(otpLength);
        Random random = new Random();

        for (int i = 0; i < otpLength; i++) {
            int index = random.nextInt(otpChars.length());
            char otpChar = otpChars.charAt(index);
            otp.append(otpChar);
        }

        return otp.toString();
    }
}
