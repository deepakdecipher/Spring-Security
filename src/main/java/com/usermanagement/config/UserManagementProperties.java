package com.usermanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Central configuration for the user-management-spring-boot-starter.
 *
 * <p>Add to your application.yaml / application.properties:
 * <pre>
 * user-management:
 *   api:
 *     user-base-path: /userApi        # default
 *     role-base-path: /roleApi        # default
 *   jwt:
 *     secret: &lt;base64-encoded-256-bit-key&gt;
 *     token-validity-ms: 1200000      # 20 min
 *     refresh-token-validity-ms: 3600000  # 1 h
 *   otp:
 *     expiration-minutes: 5
 *     length: 6
 *   email:
 *     from: no-reply@example.com
 *     subject: "Your OTP code"
 *     enabled: true
 * </pre>
 *
 * JWT secret must be at least 256 bits (32 ASCII chars) for HS256.
 * Generate one with: {@code openssl rand -base64 32}
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "user-management")
public class UserManagementProperties {

    @NestedConfigurationProperty
    private Api api = new Api();

    @NestedConfigurationProperty
    private Jwt jwt = new Jwt();

    @NestedConfigurationProperty
    private Otp otp = new Otp();

    @NestedConfigurationProperty
    private Email email = new Email();

    @NestedConfigurationProperty
    private Brevo brevo = new Brevo();

    @Getter
    @Setter
    public static class Api {
        /** Base path for user endpoints. Override to change all user API URLs at once. */
        private String userBasePath = "/userApi";
        /** Base path for role endpoints. */
        private String roleBasePath = "/roleApi";
    }

    @Getter
    @Setter
    public static class Jwt {
        /**
         * HMAC-SHA256 signing secret — must be ≥ 32 chars (256 bits).
         * Override this in production!
         */
        private String secret = "dGhpcyBpcyBhIHNlY3JldCBrZXkgZm9yIEpXVCB0b2tlbiBnZW5lcmF0aW9u";
        /** Access token validity in milliseconds. Default: 20 minutes. */
        private long tokenValidityMs = 20L * 60 * 1000;
        /** Refresh token validity in milliseconds. Default: 1 hour. */
        private long refreshTokenValidityMs = 60L * 60 * 1000;
    }

    @Getter
    @Setter
    public static class Otp {
        /** How long (minutes) an OTP remains valid. Default: 5. */
        private int expirationMinutes = 5;
        /** Number of digits in the generated OTP. Default: 6. */
        private int length = 6;
    }

    @Getter
    @Setter
    public static class Email {
        /** From address for OTP emails. Configure via spring.mail.* for SMTP. */
        private String from = "";
        /** Subject line for OTP emails. */
        private String subject = "Your OTP verification code";
        /** Set false to disable email sending (useful in dev/test). */
        private boolean enabled = true;
        /**
         * Email provider: "smtp" (uses Spring Mail) or "brevo" (uses Brevo API).
         * Default is "smtp". Set to "brevo" when SMTP port is blocked (e.g. Render free tier).
         */
        private String provider = "smtp";
    }

    @Getter
    @Setter
    public static class Brevo {
        /** Brevo (Sendinblue) API key — obtain from https://app.brevo.com/settings/keys/api */
        private String apiKey = "";
    }
}
