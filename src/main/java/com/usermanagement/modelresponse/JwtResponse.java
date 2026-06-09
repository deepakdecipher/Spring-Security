package com.usermanagement.modelresponse;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    String token;
    String refreshToken;
    Set<String> roles;
}
