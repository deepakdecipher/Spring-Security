package com.usermanagement.modelrequest;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import static com.usermanagement.constants.ValidationMessageConstants.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPassword {

    @NotBlank(message = USER_NAME_REQUIRED)
    String userName;
    @NotBlank(message = OLD_PASSWORD_REQUIRED)
    String oldPassword;
    @NotBlank(message = NEW_PASSWORD_REQUIRED)
    String newPassword;
}
