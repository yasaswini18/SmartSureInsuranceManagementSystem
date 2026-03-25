package com.InsuranceManagementSystem.AuthService.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.InsuranceManagementSystem.AuthService.entity.Role;
import com.InsuranceManagementSystem.AuthService.entity.User;
import com.InsuranceManagementSystem.AuthService.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
	 private final UserRepository repo;
	    private final PasswordEncoder encoder;

	    @Bean
	    CommandLineRunner initAdmin() {
	        return args -> {
	            if (!repo.existsByEmail("yasaswini@gmail.com")) {
	                repo.save(User.builder()
	                        .fullName("yasaswini")
	                        .email("yasaswini@gmail.com")
	                        .password(encoder.encode("yasaswini123"))
	                        .role(Role.ADMIN)
	                        .build());
	            }
	        };
	    }
}
