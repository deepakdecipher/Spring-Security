package com.usermanagement.jwt;

import com.usermanagement.exception.BadCredentialsException;
import com.usermanagement.exception.UsernameNotFoundException;
import com.usermanagement.modelrequest.JwtRequest;
import com.usermanagement.modelrequest.LoginOtpRequest;
import com.usermanagement.modelresponse.JwtResponse;
import com.usermanagement.otp.GenerateOtp;
import com.usermanagement.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import io.jsonwebtoken.JwtException;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final GenerateOtp generateOtp;

    @Override
    public JwtResponse generateToken(JwtRequest jwtRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(jwtRequest.getEmail(), jwtRequest.getPassword()));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
            throw new UsernameNotFoundException(ex.getMessage());
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(jwtRequest.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return new JwtResponse(token, refreshToken, roles);
    }

    @Override
    public JwtResponse generateTokenFromOtp(LoginOtpRequest request) {
        generateOtp.validateOtp(request.getOtp(), request.getEmail());
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return new JwtResponse(token, refreshToken, roles);
    }

    @Override
    public JwtResponse refreshToken(String refreshToken) {
        String username;
        try {
            username = jwtUtil.extractUsername(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Refresh token is invalid or expired. Please log in again.");
        }
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(refreshToken, userDetails)) {
            throw new BadCredentialsException("Refresh token is invalid or expired. Please log in again.");
        }
        String newToken = jwtUtil.generateToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return new JwtResponse(newToken, newRefreshToken, roles);
    }
}
