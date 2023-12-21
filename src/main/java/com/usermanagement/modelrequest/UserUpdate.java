package com.usermanagement.modelrequest;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import static com.usermanagement.constants.ValidationMessageConstants.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdate {

    @NotBlank(message = USER_FULL_NAME_REQUIRED)
    private String userFullName;
    @NotBlank(message = USER_NAME_REQUIRED)
    private String userName;
    @NotBlank(message = EMAIL_REQUIRED)
    private String email;
}
