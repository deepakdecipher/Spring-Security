package com.usermanagement.modelrequest;

import com.usermanagement.modelentity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

import static com.usermanagement.constants.ValidationMessageConstants.ROLES_REQUIRED;
import static com.usermanagement.constants.ValidationMessageConstants.USER_NAME_REQUIRED;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignRolesToUser {
    @NotBlank(message = USER_NAME_REQUIRED)
    String userName;
    @NotNull(message = ROLES_REQUIRED)
    Set<Role> roles;
}
