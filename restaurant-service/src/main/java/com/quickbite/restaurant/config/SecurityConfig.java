package com.quickbite.restaurant.config;

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

                        // Public endpoints for browsing restaurants.
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/all")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/city/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/cuisine/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/search")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/nearby")
                        .permitAll()

                        // Owner endpoints.
                        .requestMatchers(HttpMethod.POST, "/api/restaurants")
                        .hasRole("OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/restaurants/{id}")
                        .hasRole("OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/restaurants/toggle/**")
                        .hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/my")
                        .hasRole("OWNER")

                        // Admin endpoints.
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/pending")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/restaurants/approve/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/restaurants/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/restaurants/{id}")
                        .permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
