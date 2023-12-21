package com.usermanagement.jwt;

import com.usermanagement.modelrequest.JwtRequest;
import com.usermanagement.modelresponse.JwtResponse;

public interface JwtService {

    public JwtResponse generateToken(JwtRequest jwtRequest);
}
