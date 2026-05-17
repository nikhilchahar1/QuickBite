package com.quickbite.auth.controller;

import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.dto.LoginRequest;
import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.entity.User;
import com.quickbite.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// @RestController = @Controller + @ResponseBody
// Means: this class handles HTTP requests and returns JSON
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // @RequestBody reads the JSON body from the request
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        // ResponseEntity.status(201) = HTTP 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response); // HTTP 200 OK
    }

    // POST /api/auth/validate
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        boolean isValid = authService.validateToken(token);

        if (isValid) {
            String email = authService.extractEmailFromToken(token);
            User user = authService.getUserByEmail(email);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "userId", user.getUserId()
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false, "message", "Token invalid or expired"));
    }

    // GET /api/auth/user/{id}
    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = authService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // Returns the URL to redirect the user to Google login
    @GetMapping("/oauth2/google-url")
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl() {
        return ResponseEntity.ok(Map.of(
                "url", "http://localhost:8081/oauth2/authorization/google"
        ));
    }

    // Called if Google login fails
    @GetMapping("/oauth2/failure")
    public ResponseEntity<Map<String, String>> oauthFailure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Google login failed. Please try again."));
    }

    @PostMapping("/admin/change-role")
    public ResponseEntity<Map<String, String>> changeRole(
            @RequestBody Map<String, String> body,
            @RequestHeader("X-User-Role") String callerRole,
            @RequestHeader("X-User-Email") String callerEmail) {  // ← add caller's email

        // Only ADMIN or OWNER can change roles
        if (!callerRole.equals("ADMIN") && !callerRole.equals("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied"));
        }

        String targetEmail = body.get("email");
        String newRole = body.get("role");

        // Validate allowed roles
        if (!List.of("CUSTOMER", "OWNER", "AGENT", "ADMIN").contains(newRole)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid role: " + newRole));
        }

        // OWNER can only assign CUSTOMER or AGENT
        if (callerRole.equals("OWNER") &&
                !List.of("CUSTOMER", "AGENT").contains(newRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "OWNER can only assign CUSTOMER or AGENT roles"));
        }

        // Only ADMIN can assign ADMIN or OWNER roles
        if (List.of("ADMIN", "OWNER").contains(newRole) &&
                !callerRole.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only ADMIN can assign ADMIN or OWNER roles"));
        }

        authService.changeUserRole(targetEmail, newRole);
        return ResponseEntity.ok(Map.of(
                "message", "Role updated successfully",
                "email", targetEmail,
                "newRole", newRole
        ));
    }

}