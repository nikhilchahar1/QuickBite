package com.quickbite.auth.service;

import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.dto.LoginRequest;
import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.entity.User;
import com.quickbite.auth.repository.UserRepository;
import com.quickbite.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // These are injected by Spring automatically because of @RequiredArgsConstructor
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(RegisterRequest request) {

        // Step 1: Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Step 2: Create a new User entity
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        // Step 3: Hash the password before saving
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setPhone(request.getPhone());

        // Step 4: Set role (default to CUSTOMER if not provided)
        user.setRole(request.getRole() != null ? request.getRole() : "CUSTOMER");

        // Step 5: Save to database
        User savedUser = userRepository.save(user);

        // Step 6: Generate JWT token
        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getUserId()
        );

        // Step 7: Return response with token
        return new AuthResponse(
                token,
                savedUser.getRole(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getUserId(),
                "Registration successful"
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        // Step 1: Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Check if account is active
        if (!user.isActive()) {
            throw new RuntimeException("Account is suspended");
        }

        // Step 3: Compare the entered password with the hashed one
        // passwordEncoder.matches("plain", "hashed") → true/false
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        // Step 4: Generate JWT token
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole(),
                user.getUserId()
        );

        return new AuthResponse(
                token,
                user.getRole(),
                user.getEmail(),
                user.getFullName(),
                user.getUserId(),
                "Login successful"
        );
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }

    @Override
    public String extractEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }
}