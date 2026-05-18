package com.quickbite.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is what the server sends back to the frontend after successful login or register
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;      // JWT token
    private String role;       // CUSTOMER, OWNER, AGENT, ADMIN
    private String email;
    private String fullName;
    private Long userId;
    private String message;    // "Login successful" etc.
}