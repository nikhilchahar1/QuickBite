package com.quickbite.auth.repository;

import com.quickbite.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// JpaRepository gives us free methods:
// save(), findById(), findAll(), deleteById(), count() etc.

// JpaRepository<User(table), Long(Primary Key, userId)> means:
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring automatically writes the SQL for these
    // based on the method name!

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // SELECT * FROM users WHERE phone = ?
    Optional<User> findByPhone(String phone);
}