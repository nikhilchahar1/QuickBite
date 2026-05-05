package com.quickbite.order.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/v3/api-docs/**",
                                     "/swagger-ui/**",
                                     "/swagger-ui.html").permitAll()

                    // Customer places and manages their orders
                    .requestMatchers("/api/orders/place").hasRole("CUSTOMER")
                    .requestMatchers("/api/orders/my").hasRole("CUSTOMER")
                    .requestMatchers("/api/orders/cancel/**").hasRole("CUSTOMER")
                    .requestMatchers("/api/orders/reorder/**").hasRole("CUSTOMER")

                    // Restaurant owner manages incoming orders
                    .requestMatchers("/api/orders/restaurant/**").hasRole("OWNER")
                    .requestMatchers("/api/orders/status/**").hasAnyRole("OWNER","AGENT")

                    // Admin sees everything
                    .requestMatchers("/api/orders/all").hasRole("ADMIN")
                    .requestMatchers("/api/orders/count/**").hasRole("ADMIN")

                    .anyRequest().authenticated()
                    )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}