package com.quickbite.cart.config;

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
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()

                        // ── CUSTOMER ONLY — Cart is only for customers ──
                        .requestMatchers("/api/cart/add")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/cart/remove/**")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/cart/update-quantity")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/cart/clear")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/cart/switch-restaurant/**")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/cart/promo")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/cart")
                        .hasRole("CUSTOMER")

                        // ── ADMIN ONLY — See all carts ──
                        .requestMatchers(HttpMethod.GET, "/api/cart/all")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}