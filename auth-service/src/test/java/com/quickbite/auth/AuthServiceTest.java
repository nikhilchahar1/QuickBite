package com.quickbite.auth;

import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.entity.User;
import com.quickbite.auth.repository.UserRepository;
import com.quickbite.auth.service.AuthServiceImpl;
import com.quickbite.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) = use Mockito for this test class
// Mockito lets us create FAKE versions of dependencies
// so we don't need a real database to test
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    // @Mock creates a fake version of these classes
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    // @InjectMocks creates a real AuthServiceImpl
    // but injects the mocks above into it
    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    // @BeforeEach runs before every test method
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole("CUSTOMER");
    }

    @Test
    void register_ShouldReturnToken_WhenValidRequest() {
        // ARRANGE: Set up what the fake objects should return
        // "when userRepository.existsByEmail is called → return false"
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        // Create a fake saved user
        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setRole("CUSTOMER");
        savedUser.setFullName("Test User");
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(anyString(), anyString(), any())).thenReturn("fake.jwt.token");

        // ACT: Call the method we are testing
        AuthResponse response = authService.register(registerRequest);

        // ASSERT: Check the results
        assertNotNull(response);
        assertEquals("fake.jwt.token", response.getToken());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals("Registration successful", response.getMessage());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // ARRANGE: Email already exists in database
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // ASSERT + ACT: Expect a RuntimeException to be thrown
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Email already registered", exception.getMessage());
    }
}