package com.bookmyshow.userservice.repository;

import com.bookmyshow.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository  // Marks this as a Spring-managed repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA generates implementation automatically!
    // Method name → Query
    // findByEmail → SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // findByPhoneNumber → SELECT * FROM users WHERE phone_number = ?
    Optional<User> findByPhoneNumber(String phoneNumber);

    // existsByEmail → SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // existsByPhoneNumber → SELECT COUNT(*) > 0 FROM users WHERE phone_number = ?
    boolean existsByPhoneNumber(String phoneNumber);
}