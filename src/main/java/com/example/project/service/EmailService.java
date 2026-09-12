package com.example.project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:youremail@gmail.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        System.out.println("==================================================");
        System.out.println(">>> [OTP GENERATED] To: " + toEmail + " | Code: " + otpCode);
        System.out.println("==================================================");
        log.info(">> [OTP GENERATED] To: {}, Code: {}", toEmail, otpCode);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your OTP Verification Code");
            message.setText("Your OTP code is: " + otpCode
                    + "\nThis code will expire in 5 minutes.\nIf you didn't request this, please ignore this email.");

            System.out.println(">>> Attempting SMTP send to: " + toEmail + " from: " + fromEmail + "...");
            mailSender.send(message);
            System.out.println(">>> [SUCCESS] Email successfully delivered to " + toEmail);
            log.info(">> Email successfully sent to {}", toEmail);
        } catch (Exception e) {
            System.err.println(">>> [SMTP ERROR] Failed to send email via Gmail SMTP: " + e.getMessage());
            log.warn(">> SMTP email delivery failed: {}", e.getMessage());
        }
    }
}