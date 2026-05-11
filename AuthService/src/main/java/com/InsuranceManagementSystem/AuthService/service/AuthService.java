package com.InsuranceManagementSystem.AuthService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.InsuranceManagementSystem.AuthService.dtos.AuthResponse;
import com.InsuranceManagementSystem.AuthService.dtos.LoginRequest;
import com.InsuranceManagementSystem.AuthService.dtos.LogoutRequest;
import com.InsuranceManagementSystem.AuthService.dtos.RefreshTokenRequest;
import com.InsuranceManagementSystem.AuthService.dtos.RegisterRequest;
import com.InsuranceManagementSystem.AuthService.dtos.UserResponse;
import com.InsuranceManagementSystem.AuthService.entity.RefreshToken;
import com.InsuranceManagementSystem.AuthService.entity.Role;
import com.InsuranceManagementSystem.AuthService.entity.User;
import com.InsuranceManagementSystem.AuthService.repository.UserRepository;
import com.InsuranceManagementSystem.AuthService.security.JwtUtil;

/**
 * Service class responsible for handling user authentication, registration,
 * and token management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    /**
     * Registers a new user in the system.
     *
     * @param request the registration request containing user details
     * @return AuthResponse indicating successful registration
     * @throws RuntimeException if the email is already registered
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        // Publish event to notify other services about the new user registration
        try {
            com.InsuranceManagementSystem.AuthService.dtos.UserRegisteredMessage message = new com.InsuranceManagementSystem.AuthService.dtos.UserRegisteredMessage(
                    savedUser.getId(),
                    savedUser.getFullName(),
                    savedUser.getEmail(),
                    savedUser.getPhone(),
                    savedUser.getCreatedAt()
            );
            rabbitTemplate.convertAndSend(
                    com.InsuranceManagementSystem.AuthService.config.RabbitMQConfig.EXCHANGE,
                    com.InsuranceManagementSystem.AuthService.config.RabbitMQConfig.USER_REGISTERED_QUEUE,
                    message
            );
            log.info("Published UserRegisteredMessage for userId: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("Failed to publish UserRegisteredMessage for userId: {}", savedUser.getId(), e);
        }

        return AuthResponse.builder()
                .message("User registered successfully")
                .userId(savedUser.getId())
                .build();
    }

    /**
     * Authenticates a user and generates access and refresh tokens.
     *
     * @param request the login request containing email and password
     * @return AuthResponse containing generated tokens and user details
     * @throws RuntimeException if the user is not found or authentication fails
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return buildAuthResponse(user, refreshToken);
    }

    /**
     * Registers a new admin user in the system.
     *
     * @param request the registration request containing admin details
     * @return AuthResponse containing generated tokens and user details
     * @throws RuntimeException if the email is already registered
     */
    public AuthResponse createAdmin(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(Role.ADMIN)
                .build();

        User savedUser = userRepository.save(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);
        return buildAuthResponse(savedUser, refreshToken);
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param refreshTokenString the refresh token string
     * @return AuthResponse containing a new access token
     */
    public AuthResponse refreshAccessToken(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenString);
        User user = refreshToken.getUser();

        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .message("Access token refreshed successfully")
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtUtil.getAccessTokenExpirationMs())
                .refreshTokenExpiresIn(null)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(user.getFullName())
                .build();
    }

    /**
     * Logs out the user by revoking their refresh token.
     *
     * @param refreshTokenString the refresh token string to revoke
     */
    public void logout(String refreshTokenString) {
        if (refreshTokenString != null && !refreshTokenString.isEmpty()) {
            refreshTokenService.revokeRefreshToken(refreshTokenString);
        }
    }

    /**
     * Validates a given token and extracts the email from it.
     *
     * @param token the JWT token to validate
     * @return String the extracted email
     * @throws RuntimeException if the token is invalid or the user no longer exists
     */
    public String validateAndExtractEmail(String token) {
        String email = jwtUtil.extractEmail(token);

        if (email == null) {
            throw new RuntimeException("Invalid token");
        }

        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User no longer exists"));

        return email;
    }

    /**
     * Updates the role of a specific user.
     *
     * @param email the email of the user to update
     * @param role the new role to assign
     * @throws RuntimeException if the user is not found or the role is invalid
     */
    public void updateUserRole(String email, String role) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role newRole;
        try {
            newRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + role);
        }

        user.setRole(newRole);
        userRepository.save(user);
    }

    /**
     * Fetches user details by email.
     *
     * @param email the email of the user to fetch
     * @return UserResponse the user details
     * @throws RuntimeException if the user is not found
     */
    public UserResponse getUserByEmail(String email) {
        log.info("Admin fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                    "User not found with email: " + email
                ));

        return mapToUserResponse(user);
    }

    /**
     * Fetches all registered users.
     *
     * @return List of UserResponse containing user details
     */
    public List<UserResponse> getAllUsers() {
        log.info("Admin fetching all users");

        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Fetches users filtered by role.
     *
     * @param role the role to filter users by
     * @return List of UserResponse containing user details matching the role
     */
    public List<UserResponse> getUsersByRole(String role) {
        log.info("Admin fetching users by role: {}", role);

        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole().name().equals(role))
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Fetches the total count of registered users.
     *
     * @return long representing total number of users
     */
    public long getTotalUserCount() {
        return userRepository.count();
    }

    private AuthResponse buildAuthResponse(User user, RefreshToken refreshToken) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        long refreshTokenTtl = java.time.Duration.between(LocalDateTime.now(), refreshToken.getExpiresAt()).toMillis();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtUtil.getAccessTokenExpirationMs())
                .refreshTokenExpiresIn(Math.max(refreshTokenTtl, 0))
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(user.getFullName())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
