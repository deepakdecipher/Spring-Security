package com.usermanagement.service;

import com.usermanagement.modelrequest.AssignRolesToUser;
import com.usermanagement.modelrequest.ResetPassword;
import com.usermanagement.modelrequest.UserSignUp;
import com.usermanagement.modelrequest.UserUpdate;
import com.usermanagement.modelresponse.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse save(UserSignUp userSignUp);

    List<UserResponse> listAll();

    List<UserResponse> findUsersExceptLIU();

    UserResponse findLIU();

    UserResponse findById(long id);

    UserResponse updateUserDetails(Long id, UserUpdate userUpdate);

    List<UserResponse> findByFullName(String fullName);

    UserResponse findByUserName(String userName);

    UserResponse deleteUserById(Long id);

    String resetPassword(ResetPassword resetPassword);

    String addRoleToUser(AssignRolesToUser assignRolesToUser);
    String generateOtp(String emailId);

}
