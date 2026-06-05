package com.usermanagement.jwt;

import com.usermanagement.config.UserManagementProperties;
import com.usermanagement.exception.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT utility — builds and validates tokens using JJWT 0.12.x.
 *
 * <p>Configuration comes from {@link UserManagementProperties.Jwt}.
 * Override this bean in your project to use a different token structure.
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final UserManagementProperties properties;

    // ── Token generation ──────────────────────────────────────────────────────

    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails.getUsername(),
                properties.getJwt().getTokenValidityMs());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails.getUsername(),
                properties.getJwt().getRefreshTokenValidityMs());
    }

    private String buildToken(Map<String, Object> extra, String subject, long validityMs) {
        return Jwts.builder()
                .claims(extra)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + validityMs))
                .signWith(signingKey())
                .compact();
    }

    // ── Token validation ──────────────────────────────────────────────────────

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Claim extraction ──────────────────────────────────────────────────────

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Session expired. Please log in again.");
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private SecretKey signingKey() {
        byte[] keyBytes = properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
