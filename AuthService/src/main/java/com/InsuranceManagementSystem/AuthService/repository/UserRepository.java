package com.InsuranceManagementSystem.AuthService.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.InsuranceManagementSystem.AuthService.entity.User;

/**
 * Repository interface for User entity.
 * Provides basic CRUD operations and custom queries for User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the User if found, or empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email the email address to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
