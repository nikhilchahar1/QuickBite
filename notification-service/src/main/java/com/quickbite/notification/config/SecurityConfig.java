package com.quickbite.notification.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthFilter headerAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()

                        // ── ANY AUTHENTICATED USER ──
                        // Any logged-in user can view and manage their own notifications
                        .requestMatchers("/api/notifications/my")
                        .authenticated()
                        .requestMatchers("/api/notifications/my/unread")
                        .authenticated()
                        .requestMatchers("/api/notifications/unread-count")
                        .authenticated()
                        .requestMatchers("/api/notifications/read/**")
                        .authenticated()
                        .requestMatchers("/api/notifications/read-all")
                        .authenticated()
                        .requestMatchers("/api/notifications/delete/**")
                        .authenticated()

                        // ── ADMIN ONLY ──
                        .requestMatchers("/api/notifications/send")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/notifications/send-bulk")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/notifications/all")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}