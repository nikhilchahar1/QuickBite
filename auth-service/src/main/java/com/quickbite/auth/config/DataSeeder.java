package com.quickbite.auth.config;

import com.quickbite.auth.entity.User;
import com.quickbite.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${owner.email}")
    private String ownerEmail;

    @Value("${owner.password}")
    private String ownerPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail(ownerEmail)) {
            User owner = new User();
            owner.setEmail(ownerEmail);
            owner.setFullName("QuickBite Owner");
            owner.setPasswordHash(passwordEncoder.encode(ownerPassword));
            owner.setRole("OWNER");
            owner.setAuthProvider("LOCAL");
            userRepository.save(owner);
            System.out.println("✅ Owner account seeded: " + ownerEmail);
        }
    }
}
