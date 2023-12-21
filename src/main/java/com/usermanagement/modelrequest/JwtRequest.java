package com.usermanagement.modelrequest;

import lombok.*;
import jakarta.validation.constraints.*;

import static com.usermanagement.constants.RegexConstants.EMAIL_INVALID;
import static com.usermanagement.constants.RegexConstants.USER_EMAIL;
import static com.usermanagement.constants.ValidationMessageConstants.BLANK_EMAIL_MESSAGE;
import static com.usermanagement.constants.ValidationMessageConstants.BLANK_PASSWORD_MESSAGE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtRequest {
    @NotBlank(message = BLANK_EMAIL_MESSAGE)
    @Pattern(regexp = USER_EMAIL, message = EMAIL_INVALID)
    String email;
    @NotBlank(message = BLANK_PASSWORD_MESSAGE)
    String password;
}
