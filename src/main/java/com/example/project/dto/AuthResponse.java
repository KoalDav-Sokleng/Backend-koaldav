package com.example.project.dto;

// NOTE: reconstructed to match `new AuthResponse(token, email, firstName, lastName)`
// calls already present in AuthService.java. If your real AuthResponse.java has
// different fields/order, keep yours and ignore this one.
public class AuthResponse {

    private String token;
    private String email;
    private String firstName;
    private String lastName;

    public AuthResponse(String token, String email, String firstName, String lastName) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
