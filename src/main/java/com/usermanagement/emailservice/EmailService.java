package com.usermanagement.emailservice;

import org.springframework.mail.javamail.JavaMailSenderImpl;

public interface EmailService {
    void sendInternalServerErrorEmailNotification(String userEmail, String fromEmail, String subject, String message, JavaMailSenderImpl javaMailSender);
}

