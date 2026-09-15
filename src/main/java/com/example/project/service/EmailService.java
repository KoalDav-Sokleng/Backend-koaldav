package com.example.project.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            logger.error("Email delivery failed for recipient {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }
            message.setTo(toEmail);
            message.setSubject("Your OTP Verification Code");
            message.setText("Your OTP code is: " + otpCode
                    + "\nThis code will expire in 5 minutes.\nIf you did not request this, please ignore this email.");
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("OTP email delivery failed for recipient {}: {}", toEmail, e.getMessage(), e);
        }
    }
}