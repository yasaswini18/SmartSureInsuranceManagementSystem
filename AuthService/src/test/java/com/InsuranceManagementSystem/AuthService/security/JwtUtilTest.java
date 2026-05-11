package com.InsuranceManagementSystem.AuthService.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(
            jwtUtil, "secretKey",
            "test-secret-key-for-testing-only-must-be-long-enough"
        );
        ReflectionTestUtils.setField(
            jwtUtil, "accessTokenExpirationMs", 86400000L
        );
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void generateToken_ShouldReturnValidToken() {
        String email = "john@gmail.com";
        String role = "USER";

        String token = jwtUtil.generateToken(1L, email, role);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract correct email from token")
    void extractEmail_ShouldReturnCorrectEmail() {
        String email = "john@gmail.com";
        String token = jwtUtil.generateToken(1L, email, "USER");

        String extractedEmail = jwtUtil.extractEmail(token);

        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Should extract correct role from token")
    void extractRole_ShouldReturnCorrectRole() {
        String role = "ADMIN";
        String token = jwtUtil.generateToken(
                99L, "admin@gmail.com", role
        );

        String extractedRole = jwtUtil.extractRole(token);

        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("Should validate token successfully")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String email = "john@gmail.com";
        String token = jwtUtil.generateToken(1L, email, "USER");

        boolean isValid = jwtUtil.validateToken(token, email);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject token with wrong email")
    void validateToken_WithWrongEmail_ShouldReturnFalse() {
        String token = jwtUtil.generateToken(
                1L, "john@gmail.com", "USER"
        );

        boolean isValid = jwtUtil.validateToken(
                token, "wrong@gmail.com"
        );

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject expired token")
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        ReflectionTestUtils.setField(
                jwtUtil, "accessTokenExpirationMs", -1L
        );
        String token = jwtUtil.generateToken(
                1L, "john@gmail.com", "USER"
        );

        boolean isValid = jwtUtil.validateToken(
                token, "john@gmail.com"
        );

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should extract correct userId from token")
    void extractUserId_ShouldReturnCorrectUserId() {
        String token = jwtUtil.generateToken(42L, "john@gmail.com", "USER");

        Long extractedUserId = jwtUtil.extractUserId(token);

        assertThat(extractedUserId).isEqualTo(42L);
    }
}
