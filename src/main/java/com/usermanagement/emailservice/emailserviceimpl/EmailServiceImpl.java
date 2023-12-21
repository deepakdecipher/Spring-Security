package com.usermanagement.emailservice.emailserviceimpl;

import com.usermanagement.emailservice.EmailService;
import com.usermanagement.exception.EmailProcessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

import static com.usermanagement.emailconfig.EmailConstant.*;
@Service
@Slf4j
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendInternalServerErrorEmailNotification(String userEmail, String fromEmail, String subject, String message, JavaMailSenderImpl javaMailSender) {
        log.info("Execution started for sendNotificationEmail()");
        prepareAndSendInternalServerErrorEmailNotification(userEmail, fromEmail, subject, message, javaMailSender);
    }

    private void prepareAndSendInternalServerErrorEmailNotification(String userEmail, String fromEmail, String subject, String body, JavaMailSenderImpl javaMailSender) {

        Properties props = javaMailSender.getJavaMailProperties();
        props.put(MAIL_TRANSPORT_PROTOCOL, "smtp");
        props.put(MAIL_SMTP_AUTH, true);
        props.put(MAIL_SMTP_STARTTLS_ENABLE, true);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        prepareEmail(fromEmail, userEmail, subject, body, mimeMessage);
        sendEmail(mimeMessage, javaMailSender);
    }
    private void prepareEmail(String from, String to, String subject, String body, MimeMessage message) {
        log.trace("Prepare email body using general notification");
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
        } catch (MessagingException e) {
            throw new EmailProcessException("Something went wrong while prepare email.");
        }
    }
    private void sendEmail(MimeMessage message, JavaMailSender mailSender) {
        log.info("Sent notification email");
        mailSender.send(message);
    }
}
