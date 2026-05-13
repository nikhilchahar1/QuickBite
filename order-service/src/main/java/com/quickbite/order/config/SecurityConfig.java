package com.quickbite.order.config;

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

                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()

                        // ── CUSTOMER ONLY ──
                        .requestMatchers(HttpMethod.POST, "/api/orders/place")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/my")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/cancel/**")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/orders/reorder/**")
                        .hasRole("CUSTOMER")

                        // ── OWNER + ADMIN ──
                        .requestMatchers(HttpMethod.GET, "/api/orders/restaurant/**")
                        .hasAnyRole("OWNER", "ADMIN")

                        // ── OWNER + AGENT + ADMIN — Status update ──
                        .requestMatchers(HttpMethod.PUT, "/api/orders/status/**")
                        .hasAnyRole("OWNER", "AGENT", "ADMIN")

                        // ── AGENT ONLY — Assign and delivery ──
                        .requestMatchers(HttpMethod.PUT, "/api/orders/assign/**")
                        .hasAnyRole("AGENT", "ADMIN")

                        // ── ADMIN ONLY ──
                        .requestMatchers(HttpMethod.GET, "/api/orders/all")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/count/**")
                        .hasRole("ADMIN")

                        // Viewing a single order — any authenticated user
                        .requestMatchers(HttpMethod.GET, "/api/orders/{id}")
                        .authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}