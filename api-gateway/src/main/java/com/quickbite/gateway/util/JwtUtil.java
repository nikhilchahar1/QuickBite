package com.quickbite.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // @Value reads from application.yaml
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Converts the secret string into a cryptographic key
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String role, Long userId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date())        // when it created
                .expiration(new Date(System.currentTimeMillis() + expiration)) // when it expires
                .signWith(getSigningKey())   // sign it with our secret
                .compact();                  // build the string
    }

    // extract the email from a token
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // extracts the role from a token
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // extract the userId from a token
    public Long extractUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public boolean isTokenValid(String token) {
        try {
            return !getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // getClaims: decodes the token and returns all data inside it
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}