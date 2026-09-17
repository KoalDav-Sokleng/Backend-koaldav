package com.example.project.service;

import com.example.project.Entity.User;
import com.example.project.dto.*;
import com.example.project.repository.UserRepository;
import com.example.project.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
    }

    // 1. REGISTER: Creates user and returns JWT token immediately (no OTP needed
    // for register)
    public AuthResponse register(RegisterRequest request) {
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : "");
        user.setLastName(request.getLastName() != null ? request.getLastName().trim() : "");
        user.setEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(true);

        userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(), user.getFirstName(), user.getLastName(), false,
                "Registration successful");
    }

    // 2. LOGIN: Authenticates credentials, generates & sends 6-digit OTP to user's
    // Gmail
    public AuthResponse login(LoginRequest request) {
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(cleanEmail, request.getPassword()));

        User user = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Generate and send 6-digit OTP for login
        otpService.generateAndSendOtp(user.getEmail());

        return new AuthResponse(null, user.getEmail(), user.getFirstName(), user.getLastName(), true,
                "OTP sent to your Gmail account. Please enter the 6-digit code to continue.");
    }

    // 2b. VERIFY LOGIN OTP: Validates OTP and returns JWT token
    public AuthResponse verifyLoginOtp(VerifyOtpRequest request) {
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        boolean isValid = otpService.verifyOtp(cleanEmail, request.getOtpCode(), "LOGIN");
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(), user.getFirstName(), user.getLastName(), false,
                "Login successful");
    }

    // 3. FORGOT PASSWORD: Sends 6-digit OTP code to user's Gmail
    public void forgotPassword(ForgotPasswordRequest request) {
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (!userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new IllegalArgumentException("No account found with this email");
        }
        otpService.generateAndSendOtp(cleanEmail, "RESET_PASSWORD");
    }

    // 3b. VERIFY OTP: Validates OTP code
    public boolean verifyOtp(VerifyOtpRequest request) {
        // Do not mark the OTP used here. The next screen submits it to
        // resetPassword, which validates and consumes it exactly once.
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        return otpService.isOtpValid(cleanEmail, request.getOtpCode(), "RESET_PASSWORD");
    }

    // 4. RESET PASSWORD: Validates OTP and sets new password, then returns JWT for
    // immediate dashboard redirect
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        boolean isValid = otpService.verifyOtp(cleanEmail, request.getOtpCode(), "RESET_PASSWORD");
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // Persist the BCrypt hash before generating a session. Flushing here
        // makes the password change immediately visible to the next login.
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.saveAndFlush(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();

        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token, user.getEmail(), user.getFirstName(), user.getLastName(), false,
                "Password reset successful");
    }
}
