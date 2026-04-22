package com.quickbite.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// @Entity tells JPA: "Create a table for this class"
@Entity
@Table(name = "users")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    // We store hashed password, never plain text
    @Column(nullable = false)
    private String passwordHash;

    @Column
    private String phone;

    // Role decides what the user can do:
    // CUSTOMER, OWNER, AGENT, ADMIN
    @Column(nullable = false)
    private String role;

    // Is the account active? (not deleted/suspended)
    @Column(nullable = false)
    private boolean isActive = true;

    // When was the account created?
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private String profilePicUrl;

    // @PrePersist runs this method automatically
    // just before the record is saved to the database
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = "CUSTOMER";
        }
    }
}