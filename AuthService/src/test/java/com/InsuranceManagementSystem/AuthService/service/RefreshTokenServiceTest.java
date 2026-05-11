package com.InsuranceManagementSystem.AuthService.service;

import com.InsuranceManagementSystem.AuthService.entity.RefreshToken;
import com.InsuranceManagementSystem.AuthService.entity.Role;
import com.InsuranceManagementSystem.AuthService.entity.User;
import com.InsuranceManagementSystem.AuthService.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 604800000L);
        ReflectionTestUtils.setField(refreshTokenService, "inactivityTimeoutMs", 1800000L);

        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("Should create refresh token")
    void createRefreshToken_ShouldReturnNewToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        RefreshToken token = refreshTokenService.createRefreshToken(mockUser);

        assertThat(token).isNotNull();
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getUser()).isEqualTo(mockUser);
        verify(refreshTokenRepository).deleteByUser(mockUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should validate valid refresh token")
    void validateRefreshToken_WithValidToken_ShouldReturnToken() {
        RefreshToken validToken = RefreshToken.builder()
                .token("valid-token")
                .user(mockUser)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .lastUsedAt(LocalDateTime.now().minusMinutes(5))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        RefreshToken validated = refreshTokenService.validateRefreshToken("valid-token");

        assertThat(validated).isNotNull();
        verify(refreshTokenRepository).save(validToken);
    }

    @Test
    @DisplayName("Should throw exception if token not found")
    void validateRefreshToken_NotFound_ShouldThrowException() {
        when(refreshTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("invalid"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    @DisplayName("Should throw exception if token revoked")
    void validateRefreshToken_Revoked_ShouldThrowException() {
        RefreshToken revokedToken = RefreshToken.builder()
                .token("revoked")
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken("revoked")).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("revoked"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh token has been revoked");
    }

    @Test
    @DisplayName("Should throw exception if token expired")
    void validateRefreshToken_Expired_ShouldThrowException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("expired"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh token has expired");
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("Should throw exception if token inactive")
    void validateRefreshToken_Inactive_ShouldThrowException() {
        RefreshToken inactiveToken = RefreshToken.builder()
                .token("inactive")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .lastUsedAt(LocalDateTime.now().minusMinutes(60)) // > 30 minutes
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("inactive")).thenReturn(Optional.of(inactiveToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("inactive"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Session expired due to inactivity");
        verify(refreshTokenRepository).save(inactiveToken);
    }

    @Test
    @DisplayName("Should revoke token")
    void revokeRefreshToken_ShouldSetRevoked() {
        RefreshToken token = RefreshToken.builder()
                .token("to-revoke")
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("to-revoke")).thenReturn(Optional.of(token));

        refreshTokenService.revokeRefreshToken("to-revoke");

        verify(refreshTokenRepository).save(token);
        assertThat(token.getRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should revoke all for user")
    void revokeAllForUser_ShouldDelete() {
        refreshTokenService.revokeAllForUser(mockUser);
        verify(refreshTokenRepository).deleteByUser(mockUser);
    }

    @Test
    @DisplayName("Should purge expired tokens")
    void purgeExpiredRefreshTokens_ShouldDeleteBeforeNow() {
        refreshTokenService.purgeExpiredRefreshTokens();
        verify(refreshTokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}
