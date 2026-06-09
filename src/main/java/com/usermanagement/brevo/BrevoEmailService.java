package com.usermanagement.brevo;

import com.usermanagement.config.UserManagementProperties;
import com.usermanagement.emailservice.EmailService;
import com.usermanagement.exception.EmailProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Email delivery via the Brevo (formerly Sendinblue) Transactional Email API v3.
 *
 * Activated when {@code user-management.email.provider=brevo}.
 * Requires {@code user-management.brevo.api-key} to be set.
 *
 * To remove Brevo support: delete this package and update
 * {@link com.usermanagement.autoconfigure.UserManagementAutoConfiguration}.
 */
@Slf4j
@RequiredArgsConstructor
public class BrevoEmailService implements EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final UserManagementProperties properties;

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

        String apiKey = properties.getBrevo().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new EmailProcessException("Brevo API key is not configured (user-management.brevo.api-key)");
        }

        String from = (fromEmail != null && !fromEmail.isBlank())
                ? fromEmail
                : properties.getEmail().getFrom();

        String body = """
                {
                  "sender": { "email": "%s", "name": "Nutro Assist" },
                  "to": [ { "email": "%s" } ],
                  "subject": "%s",
                  "htmlContent": %s
                }
                """.formatted(
                escapeJson(from),
                escapeJson(userEmail),
                escapeJson(subject),
                toJsonString(message)
        );

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_API_URL))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Brevo API error {} sending to {}: {}", response.statusCode(), userEmail, response.body());
                throw new EmailProcessException("Failed to send verification email. Please try again later.");
            }
            log.debug("Email sent via Brevo to {}", userEmail);
        } catch (EmailProcessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Brevo email error for {}: {}", userEmail, e.getMessage());
            throw new EmailProcessException("Failed to send verification email. Please try again later.");
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String toJsonString(String s) {
        return "\"" + escapeJson(s) + "\"";
    }
}
