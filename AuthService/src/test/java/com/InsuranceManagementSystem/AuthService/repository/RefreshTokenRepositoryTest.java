package com.InsuranceManagementSystem.AuthService.repository;

import com.InsuranceManagementSystem.AuthService.entity.RefreshToken;
import com.InsuranceManagementSystem.AuthService.entity.Role;
import com.InsuranceManagementSystem.AuthService.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password")
                .phone("1234567890")
                .address("Some Address")
                .role(Role.USER)
                .build();
        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("Should find token by string")
    void findByToken_ShouldReturnToken() {
        RefreshToken token = RefreshToken.builder()
                .token("test-token")
                .user(savedUser)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        refreshTokenRepository.save(token);

        Optional<RefreshToken> found = refreshTokenRepository.findByToken("test-token");

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("Should delete token by user")
    void deleteByUser_ShouldDeleteToken() {
        RefreshToken token = RefreshToken.builder()
                .token("test-token")
                .user(savedUser)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        refreshTokenRepository.save(token);

        refreshTokenRepository.deleteByUser(savedUser);
        
        Optional<RefreshToken> found = refreshTokenRepository.findByToken("test-token");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should delete tokens expired before cutoff")
    void deleteByExpiresAtBefore_ShouldDeleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .user(savedUser)
                .expiresAt(now.minusDays(1))
                .build();
        refreshTokenRepository.save(expiredToken);

        RefreshToken validToken = RefreshToken.builder()
                .token("valid-token")
                .user(savedUser)
                .expiresAt(now.plusDays(1))
                .build();
        refreshTokenRepository.save(validToken);

        refreshTokenRepository.deleteByExpiresAtBefore(now);

        assertThat(refreshTokenRepository.findByToken("expired-token")).isEmpty();
        assertThat(refreshTokenRepository.findByToken("valid-token")).isPresent();
    }
}
