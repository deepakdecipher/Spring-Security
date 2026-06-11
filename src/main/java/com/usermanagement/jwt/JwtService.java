package com.usermanagement.jwt;

import com.usermanagement.modelrequest.JwtRequest;
import com.usermanagement.modelrequest.LoginOtpRequest;
import com.usermanagement.modelresponse.JwtResponse;

public interface JwtService {

    JwtResponse generateToken(JwtRequest jwtRequest);

    JwtResponse generateTokenFromOtp(LoginOtpRequest request);

    JwtResponse refreshToken(String refreshToken);
}
