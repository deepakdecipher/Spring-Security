package com.usermanagement.emailconfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Legacy email configuration — kept for backward compatibility.
 * Prefer {@code user-management.email.*} properties via {@link com.usermanagement.config.UserManagementProperties}.
 */
@Getter
@Setter
@Configuration
public class EmailNotificationProperties {

    @Value("${email-configuration.smtp-host:smtp.gmail.com}")
    private String smtpHost;

    @Value("${email-configuration.smtp-port:587}")
    private Integer smtpPort;

    @Value("${email-configuration.smtp-username:}")
    private String smtpUsername;

    @Value("${email-configuration.smtp-password:}")
    private String smtpPassword;

    @Value("${email-configuration.smtp-email-from:}")
    private String smtpEmailFrom;

    @Value("${email-configuration.smtp-email-subject:OTP Verification}")
    private String smtpEmailSubject;

    @Value("${email-configuration.username:}")
    private String userName;

    @Value("${email-configuration.email:}")
    private String email;

    @Value("${email-configuration.otp-length:6}")
    private Integer otpLength;
}
