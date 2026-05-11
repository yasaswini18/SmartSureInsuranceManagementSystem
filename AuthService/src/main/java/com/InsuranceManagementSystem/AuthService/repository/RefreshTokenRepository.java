package com.InsuranceManagementSystem.AuthService.repository;

import com.InsuranceManagementSystem.AuthService.entity.RefreshToken;
import com.InsuranceManagementSystem.AuthService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity.
 * Provides basic CRUD operations and custom queries for RefreshToken.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its token string.
     *
     * @param token the token string to search for
     * @return an Optional containing the RefreshToken if found, or empty otherwise
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes all refresh tokens associated with a specific user.
     *
     * @param user the User whose refresh tokens should be deleted
     */
    void deleteByUser(User user);

    /**
     * Deletes all refresh tokens that expired before the given cutoff date and time.
     *
     * @param cutoff the date and time before which tokens should be deleted
     */
    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}
