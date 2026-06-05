package com.usermanagement.emailservice.emailserviceimpl;

import com.usermanagement.config.UserManagementProperties;
import com.usermanagement.emailservice.EmailService;
import com.usermanagement.exception.EmailProcessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Default email service using Spring Boot's auto-configured {@link JavaMailSender}.
 *
 * Configure SMTP in {@code application.yml}:
 * <pre>
 * spring:
 *   mail:
 *     host: smtp.gmail.com
 *     port: 587
 *     username: your@email.com
 *     password: app-password
 *     properties.mail.smtp.starttls.enable: true
 * </pre>
 *
 * Override this bean to use a different email provider or template engine.
 */
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final UserManagementProperties properties;

    /**
     * Sends an HTML email. The {@code javaMailSender} parameter is accepted for
     * interface compatibility but ignored — the auto-configured {@link JavaMailSender}
     * is always used instead.
     */
    @Override
    public void sendInternalServerErrorEmailNotification(
            String userEmail,
            String fromEmail,
            String subject,
            String message,
            JavaMailSenderImpl javaMailSender) {

        if (!properties.getEmail().isEnabled()) {
            log.info("Email disabled. Would have sent to {}: {}", userEmail, subject);
            return;
        }

        try {
            JavaMailSender sender = mailSender.getIfAvailable();
            if (sender == null) {
                throw new EmailProcessException("Email is enabled, but no JavaMailSender is configured");
            }

            MimeMessage mimeMessage = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail.isBlank() ? properties.getEmail().getFrom() : fromEmail);
            helper.setTo(userEmail);
            helper.setSubject(subject);
            helper.setText(message, true);
            sender.send(mimeMessage);
            log.debug("Email sent to {}", userEmail);
        } catch (MessagingException e) {
            throw new EmailProcessException("Failed to send email to " + userEmail);
        }
    }

    /** Convenience method — builds and sends an OTP email. */
    public void sendOtpEmail(String toEmail, String otp) {
        sendInternalServerErrorEmailNotification(
                toEmail,
                properties.getEmail().getFrom(),
                properties.getEmail().getSubject(),
                buildOtpBody(otp),
                null
        );
    }

    private String buildOtpBody(String otp) {
        return """
                <html>
                <body style="font-family:Arial,sans-serif;padding:20px;">
                  <h2 style="color:#2c3e50;">OTP Verification</h2>
                  <p>Your one-time password is:</p>
                  <h1 style="letter-spacing:8px;color:#e74c3c;">%s</h1>
                  <p>Valid for <strong>%d minute(s)</strong>.</p>
                  <p style="color:#7f8c8d;font-size:12px;">If you did not request this, ignore this email.</p>
                </body>
                </html>
                """.formatted(otp, properties.getOtp().getExpirationMinutes());
    }
}
