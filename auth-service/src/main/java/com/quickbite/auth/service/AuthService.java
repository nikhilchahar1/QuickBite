package com.quickbite.auth.service;

import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.dto.LoginRequest;
import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.entity.User;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    User getUserByEmail(String email);

    User getUserById(Long userId);

    boolean validateToken(String token);

    String extractEmailFromToken(String token);

    void changeUserRole(String email, String newRole);
}