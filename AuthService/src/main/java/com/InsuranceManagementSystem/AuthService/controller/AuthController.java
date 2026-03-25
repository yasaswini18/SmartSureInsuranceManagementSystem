package com.InsuranceManagementSystem.AuthService.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.InsuranceManagementSystem.AuthService.dtos.AuthResponse;
import com.InsuranceManagementSystem.AuthService.dtos.LoginRequest;
import com.InsuranceManagementSystem.AuthService.dtos.RegisterRequest;
import com.InsuranceManagementSystem.AuthService.repository.UserRepository;
import com.InsuranceManagementSystem.AuthService.security.JwtUtil;
import com.InsuranceManagementSystem.AuthService.service.AuthService;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> createAdmin(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.createAdmin(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Auth Service is running ✅");
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validate(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        String email = authService.validateAndExtractEmail(token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Token is valid for: " + email);
    }
   
}