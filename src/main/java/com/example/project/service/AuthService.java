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

@Service
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

    // 1. REGISTER: Creates user and returns JWT token immediately (no OTP needed for register)
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(true);

        userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(), user.getFirstName(), user.getLastName(), false, "Registration successful");
    }

    // 2. LOGIN: Authenticates credentials, generates & sends 6-digit OTP to user's Gmail
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Generate and send 6-digit OTP for login
        otpService.generateAndSendOtp(user.getEmail());

        return new AuthResponse(null, user.getEmail(), user.getFirstName(), user.getLastName(), true, "OTP sent to your Gmail account. Please enter the 6-digit code to continue.");
    }

    // 2b. VERIFY LOGIN OTP: Validates OTP and returns JWT token
    public AuthResponse verifyLoginOtp(VerifyOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(), user.getFirstName(), user.getLastName(), false, "Login successful");
    }

    // 3. FORGOT PASSWORD: Sends 6-digit OTP code to user's Gmail
    public void forgotPassword(ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("No account found with this email");
        }
        otpService.generateAndSendOtp(request.getEmail());
    }

    // 3b. VERIFY OTP: Validates OTP code
    public boolean verifyOtp(VerifyOtpRequest request) {
        return otpService.verifyOtp(request.getEmail(), request.getOtpCode());
    }

    // 4. RESET PASSWORD: Validates OTP and sets new password
    public void resetPassword(ResetPasswordRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}