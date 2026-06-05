package com.usermanagement.otp;

import com.google.common.cache.LoadingCache;
import com.usermanagement.config.UserManagementProperties;
import com.usermanagement.exception.OtpExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;

/**
 * OTP generation and validation backed by Guava's {@link LoadingCache}.
 *
 * <p>The cache expires entries automatically after the duration set in
 * {@code user-management.otp.expiration-minutes} (default 5 min).
 * OTPs are single-use — invalidated immediately after successful validation.
 *
 * <p>Override this bean to use Redis, a database, or any other store.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateOtp {

    private final LoadingCache<String, String> otpLoadingCache;
    private final UserManagementProperties properties;
    private final SecureRandom random = new SecureRandom();

    // ── Generation ────────────────────────────────────────────────────────────

    /**
     * Generates a new OTP for the given email, invalidating any existing one.
     *
     * @return the generated OTP string
     */
    public String generateOtp(String email) {
        otpLoadingCache.invalidate(email);
        String otp = numericOtp(properties.getOtp().getLength());
        otpLoadingCache.put(email, otp);
        log.debug("OTP generated for {}", email);
        return otp;
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Validates the OTP for the given email, then invalidates it (single-use).
     *
     * @throws OtpExpiredException if the OTP has expired, does not match, or was not found
     */
    public void validateOtp(String otp, String email) {
        try {
            String cached = otpLoadingCache.get(email);
            if (!cached.equals(otp)) {
                throw new OtpExpiredException("Invalid OTP. Please check and try again.");
            }
            otpLoadingCache.invalidate(email); // single-use
        } catch (ExecutionException e) {
            throw new OtpExpiredException("OTP expired or not found. Please request a new one.");
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String numericOtp(int length) {
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        return String.valueOf(min + random.nextInt(max - min + 1));
    }
}
