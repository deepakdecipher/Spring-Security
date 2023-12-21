package com.usermanagement.jwt;

import com.usermanagement.modelrequest.JwtRequest;
import com.usermanagement.modelresponse.JwtResponse;
import com.usermanagement.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public JwtResponse generateToken(JwtRequest jwtRequest) {
        try {
            return extractToken(jwtRequest);
        } catch (Exception ex) {
           throw new BadCredentialsException(ex.getMessage());
        }
    }

    public JwtResponse extractToken(JwtRequest jwtRequest) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(jwtRequest.getEmail(), jwtRequest.getPassword()));

        } catch (UsernameNotFoundException ex) {
            throw new com.usermanagement.exception.UsernameNotFoundException(ex.getMessage());
        } catch (BadCredentialsException ex) {
            throw new com.usermanagement.exception.BadCredentialsException("Email or password is invalid. Please try with correct credentials.");
        }
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(jwtRequest.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        System.out.println("Generated token for username: " + userDetails.getUsername() + "-> " + token);
        return new JwtResponse(token,refreshToken);
    }


}
