package com.usermanagement.modelrequest;

import com.usermanagement.modelentity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

import static com.usermanagement.constants.ValidationMessageConstants.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUp {
    @NotBlank(message = USER_FULL_NAME_REQUIRED)
    private String userFullName;
    @NotBlank(message = USER_NAME_REQUIRED)
    private String userName;
    @NotBlank(message = PASSWORD_REQUIRED)
    public String password;
    @NotBlank(message = EMAIL_REQUIRED)
    private String email;
    @NotNull(message = ROLES_REQUIRED)
    private Set<Role> role;
}
