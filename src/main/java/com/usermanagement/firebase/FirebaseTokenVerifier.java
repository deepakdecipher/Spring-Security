package com.usermanagement.firebase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.config.UserManagementProperties;
import com.usermanagement.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Verifies Firebase Authentication ID tokens issued for phone sign-in.
 *
 * Uses Firebase's public JWKS endpoint to validate RS256 signatures — no
 * Firebase Admin SDK or service account credentials required; only the
 * Firebase project ID is needed ({@code user-management.firebase.project-id}).
 */
@Slf4j
@Component
public class FirebaseTokenVerifier {

    private static final String JWKS_URL =
            "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";

    private final String projectId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile Map<String, PublicKey> cachedKeys = Map.of();
    private volatile Instant cacheExpiry = Instant.EPOCH;

    public FirebaseTokenVerifier(UserManagementProperties properties) {
        this.projectId = properties.getFirebase().getProjectId();
    }

    /**
     * Verifies a Firebase ID token and returns the verified E.164 phone number.
     *
     * @param idToken Firebase ID token from the client (after phone OTP confirmation)
     * @return E.164 phone number (e.g. {@code +919876543210})
     * @throws InvalidTokenException if the token is invalid, expired, or not a phone token
     */
    public String verifyAndGetPhoneNumber(String idToken) {
        if (projectId == null || projectId.isBlank()) {
            throw new InvalidTokenException("Firebase project ID is not configured (user-management.firebase.project-id).");
        }

        Map<String, PublicKey> keys = getKeys();
        String kid = extractKid(idToken);
        PublicKey key = keys.get(kid);
        if (key == null) {
            // Key rotation — refresh once and retry
            keys = refreshKeys();
            key = keys.get(kid);
        }
        if (key == null) {
            throw new InvalidTokenException("Unknown Firebase signing key ID: " + kid);
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireAudience(projectId)
                    .requireIssuer("https://securetoken.google.com/" + projectId)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();

            String phone = claims.get("phone_number", String.class);
            if (phone == null || phone.isBlank()) {
                throw new InvalidTokenException("Firebase token does not contain a phone number.");
            }
            return phone;
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Firebase token verification failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid or expired Firebase token.");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String extractKid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) throw new InvalidTokenException("Malformed Firebase token.");
            String padded = parts[0] + "=".repeat((4 - parts[0].length() % 4) % 4);
            String header = new String(Base64.getUrlDecoder().decode(padded), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(header);
            if (!node.has("kid")) throw new InvalidTokenException("Firebase token is missing 'kid' header.");
            return node.get("kid").asText();
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid Firebase token format.");
        }
    }

    private synchronized Map<String, PublicKey> getKeys() {
        if (Instant.now().isBefore(cacheExpiry)) return cachedKeys;
        return refreshKeys();
    }

    private synchronized Map<String, PublicKey> refreshKeys() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(JWKS_URL))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Map<String, PublicKey> keys = parseJwks(response.body());

            // Honour Cache-Control max-age from Google (typically 6 h); fallback 1 h
            long maxAge = 3600L;
            String cc = response.headers().firstValue("Cache-Control").orElse("");
            if (cc.contains("max-age=")) {
                try {
                    maxAge = Long.parseLong(cc.split("max-age=")[1].split("[,\\s]")[0].trim());
                } catch (NumberFormatException ignored) {}
            }

            cachedKeys = keys;
            cacheExpiry = Instant.now().plusSeconds(maxAge);
            log.debug("Firebase JWKS refreshed: {} keys, valid for {}s", keys.size(), maxAge);
            return keys;
        } catch (Exception e) {
            log.error("Failed to fetch Firebase JWKS: {}", e.getMessage());
            if (!cachedKeys.isEmpty()) {
                log.warn("Using stale Firebase JWKS cache.");
                return cachedKeys;
            }
            throw new InvalidTokenException("Cannot fetch Firebase public keys: " + e.getMessage());
        }
    }

    private Map<String, PublicKey> parseJwks(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode keysNode = root.get("keys");
        Map<String, PublicKey> result = new HashMap<>();
        KeyFactory kf = KeyFactory.getInstance("RSA");
        for (JsonNode jwk : keysNode) {
            String kid = jwk.get("kid").asText();
            BigInteger modulus  = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.get("n").asText()));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.get("e").asText()));
            result.put(kid, kf.generatePublic(new RSAPublicKeySpec(modulus, exponent)));
        }
        return result;
    }
}
