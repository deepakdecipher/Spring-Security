package com.usermanagement.jwt;

import com.usermanagement.exception.BadCredentialsException;
import com.usermanagement.exception.UsernameNotFoundException;
import com.usermanagement.modelrequest.JwtRequest;
import com.usermanagement.modelresponse.JwtResponse;
import com.usermanagement.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;

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
        return new JwtResponse(token, refreshToken);
    }
}
