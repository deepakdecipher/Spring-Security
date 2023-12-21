package com.usermanagement.modelresponse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    String token;
    String refreshToken;
}
