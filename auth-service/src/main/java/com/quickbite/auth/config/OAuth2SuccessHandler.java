package com.quickbite.auth.config;

import com.quickbite.auth.entity.User;
import com.quickbite.auth.repository.UserRepository;
import com.quickbite.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private static final String FRONTEND_REDIRECT = "http://localhost:4200/oauth2/callback";

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract Google profile
        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        log.info("OAuth2 success handler triggered for: {}", email);

        // Find or create user — all in one transaction here
        User user = userRepository.findByGoogleId(googleId).orElse(null);

        if (user == null) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user == null) {
            // First time Google login — create new user
            user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setRole("CUSTOMER");
            user.setPasswordHash(null);
            log.info("Creating new user from Google: {}", email);
        }

        // Update Google fields
        user.setGoogleId(googleId);
        user.setAuthProvider("GOOGLE");
        if (picture != null) user.setProfilePicUrl(picture);

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User saved with ID: {}", savedUser.getUserId());

        // Generate QuickBite JWT
        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getUserId()
        );

        log.info("JWT generated successfully for: {}", email);

        // Redirect to frontend with token
        getRedirectStrategy().sendRedirect(request, response,
                FRONTEND_REDIRECT + "?token=" + token + "&role=" + savedUser.getRole());
    }
}