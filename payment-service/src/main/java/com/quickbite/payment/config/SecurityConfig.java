package com.quickbite.payment.config;

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
                .sessionManagement(s -> s.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()

                        // CUSTOMER — process payment, wallet ops
                        .requestMatchers(HttpMethod.POST, "/api/payments/process")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/payments/refund/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payments/order/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/my")
                        .hasRole("CUSTOMER")

                        // WALLET — customer only
                        .requestMatchers("/api/wallet/balance")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/wallet/add")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/wallet/pay")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/wallet/statements")
                        .hasRole("CUSTOMER")

                        // ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/payments/all")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}