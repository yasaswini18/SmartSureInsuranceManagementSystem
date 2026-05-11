package com.InsuranceManagementSystem.AuthService.service;

import com.InsuranceManagementSystem.AuthService.entity.RefreshToken;
import com.InsuranceManagementSystem.AuthService.entity.User;
import com.InsuranceManagementSystem.AuthService.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service class responsible for managing refresh tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh.token.expiration:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${auth.inactivity-timeout:1800000}")
    private long inactivityTimeoutMs;

    /**
     * Creates a new refresh token for the specified user.
     * Deletes any existing refresh tokens for the user before creating a new one.
     *
     * @param user the user for whom the refresh token is created
     * @return the created RefreshToken
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Validates a refresh token by checking its existence, revocation status,
     * expiration, and inactivity timeout.
     *
     * @param token the refresh token string to validate
     * @return the valid RefreshToken
     * @throws RuntimeException if the token is invalid, revoked, expired, or inactive
     */
    @Transactional
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token has expired");
        }

        LocalDateTime inactivityCutoff = LocalDateTime.now().minusNanos(inactivityTimeoutMs * 1_000_000);
        if (refreshToken.getLastUsedAt() != null && refreshToken.getLastUsedAt().isBefore(inactivityCutoff)) {
            refreshToken.setRevoked(true);
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
            throw new RuntimeException("Session expired due to inactivity");
        }

        refreshToken.setLastUsedAt(LocalDateTime.now());
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revokes a specific refresh token.
     *
     * @param token the refresh token string to revoke
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
        });
    }

    /**
     * Revokes all refresh tokens for a specific user by deleting them.
     *
     * @param user the user whose refresh tokens should be deleted
     */
    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * Scheduled task to periodically purge expired refresh tokens from the database.
     * Runs at the top of every hour.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.debug("Expired refresh tokens cleaned up");
    }
}
