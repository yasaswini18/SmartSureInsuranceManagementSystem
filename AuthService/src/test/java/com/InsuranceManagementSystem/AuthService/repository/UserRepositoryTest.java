package com.InsuranceManagementSystem.AuthService.repository;

import com.InsuranceManagementSystem.AuthService.entity.Role;
import com.InsuranceManagementSystem.AuthService.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and find user by email")
    void saveAndFindByEmail_ShouldReturnUser() {
        User user = User.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password")
                .phone("1234567890")
                .address("Some Address")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void existsByEmail_ShouldReturnTrueIfExists() {
        User user = User.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password")
                .phone("1234567890")
                .address("Some Address")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail should return false if not exists")
    void existsByEmail_ShouldReturnFalseIfNotExists() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }
}
