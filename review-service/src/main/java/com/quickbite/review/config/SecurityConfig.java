package com.quickbite.review.config;

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

                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()

                        // PUBLIC — Anyone can read reviews
                        .requestMatchers(HttpMethod.GET, "/api/reviews/restaurant/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/agent/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/avg/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/order/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/{id}")
                        .permitAll()

                        // Only customers submit reviews
                        .requestMatchers(HttpMethod.POST, "/api/reviews")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/my")
                        .hasRole("CUSTOMER")
                        // Customer can update their own review
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/**")
                        .hasRole("CUSTOMER")

                        // ── ADMIN ONLY ──
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/verify/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/all")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}