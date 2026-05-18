package com.quickbite.menu.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Public - Anyone can view menus
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET, "/api/menu/**"
                        ).permitAll()

                        // ── OWNER ONLY — Menu management ──
                        .requestMatchers(
                                HttpMethod.POST, "/api/menu/category"
                        ).hasRole("OWNER")
                        .requestMatchers(
                                HttpMethod.POST, "/api/menu/item"
                        ).hasRole("OWNER")
                        .requestMatchers(
                                HttpMethod.PUT, "/api/menu/items/**"
                        ).hasRole("OWNER")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/menu/items/**"
                        ).hasRole("OWNER")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/menu/category/**"
                        ).hasRole("OWNER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}