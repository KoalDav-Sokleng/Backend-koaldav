package com.example.project.service;

import com.example.project.Entity.PasswordResetOtp;
import com.example.project.repository.PasswordResetOtpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional
public class OtpService {

    private final PasswordResetOtpRepository otpRepository;
    private final EmailService emailService;
    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 3;

    public OtpService(PasswordResetOtpRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void generateAndSendOtp(String email) {
        generateAndSendOtp(email, "LOGIN");
    }

    @Transactional
    public void generateAndSendOtp(String email, String purpose) {
        // A resend replaces every previous code for this operation. This avoids
        // ambiguity when several emails arrive out of order.
        otpRepository.invalidateActiveOtps(email, purpose);

        String otpCode = generateOtp();

        PasswordResetOtp otp = new PasswordResetOtp();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setPurpose(purpose);
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        otpRepository.save(otp);

        emailService.sendOtpEmail(email, otpCode);
    }

    public boolean verifyOtp(String email, String otpCode) {
        return verifyOtp(email, otpCode, "RESET_PASSWORD");
    }

    public boolean verifyOtp(String email, String otpCode, String purpose) {
        String cleanEmail = email != null ? email.trim() : "";
        return otpRepository.findTopByEmailIgnoreCaseAndPurposeAndUsedFalseOrderByIdDesc(cleanEmail, purpose)
                .filter(otp -> matchesValidOtp(otp, otpCode))
                .map(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Checks a code without consuming it. The password-reset screen first calls
     * /verify-otp, then sends the same code to /reset-password; consuming it in
     * the first request made the second request always fail.
     */
    public boolean isOtpValid(String email, String otpCode, String purpose) {
        String cleanEmail = email != null ? email.trim() : "";
        return otpRepository.findTopByEmailIgnoreCaseAndPurposeAndUsedFalseOrderByIdDesc(cleanEmail, purpose)
                .map(otp -> matchesValidOtp(otp, otpCode))
                .orElse(false);
    }

    private boolean matchesValidOtp(PasswordResetOtp otp, String otpCode) {
        return otp.getOtpCode().equals(otpCode)
                && otp.getExpiryTime().isAfter(LocalDateTime.now());
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // 6-digit number
        return String.valueOf(otp);
    }
}
