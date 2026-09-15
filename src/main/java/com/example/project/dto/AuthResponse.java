package com.example.project.dto;

public class AuthResponse {

    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private boolean otpRequired;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email, String firstName, String lastName) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.otpRequired = false;
    }

    public AuthResponse(String token, String email, String firstName, String lastName, boolean otpRequired, String message) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.otpRequired = otpRequired;
        this.message = message;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public boolean isOtpRequired() { return otpRequired; }
    public void setOtpRequired(boolean otpRequired) { this.otpRequired = otpRequired; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
