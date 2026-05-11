package com.InsuranceManagementSystem.AuthService.service;

import com.InsuranceManagementSystem.AuthService.dtos.AuthResponse;
import com.InsuranceManagementSystem.AuthService.dtos.LoginRequest;
import com.InsuranceManagementSystem.AuthService.dtos.RegisterRequest;
import com.InsuranceManagementSystem.AuthService.entity.RefreshToken;
import com.InsuranceManagementSystem.AuthService.entity.User;
import com.InsuranceManagementSystem.AuthService.entity.Role;
import com.InsuranceManagementSystem.AuthService.repository.UserRepository;
import com.InsuranceManagementSystem.AuthService.security.JwtUtil;
import com.InsuranceManagementSystem.AuthService.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "John Customer",
                "john@gmail.com",
                "John@123",
                "9876543210",
                "Hyderabad"
        );

        loginRequest = new LoginRequest(
                "john@gmail.com",
                "John@123"
        );

        mockUser = User.builder()
                .id(1L)
                .fullName("John Customer")
                .email("john@gmail.com")
                .password("$2a$10$encodedPassword")
                .phone("9876543210")
                .address("Hyderabad")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void register_WithValidRequest_ShouldReturnAuthResponse() {
        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("John@123"))
                .thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(mockUser);
        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("User registered successfully");
        assertThat(response.getUserId()).isEqualTo(1L);

        verify(userRepository).existsByEmail("john@gmail.com");
        verify(passwordEncoder).encode("John@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void register_WithDuplicateEmail_ShouldThrowException() {
        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(registerRequest)
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login user successfully")
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(null);

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateAccessToken(1L, "john@gmail.com", "USER"))
                .thenReturn("mock.jwt.token");
        when(jwtUtil.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(refreshTokenService.createRefreshToken(mockUser)).thenReturn(
                RefreshToken.builder()
                        .token("refresh-token-123")
                        .user(mockUser)
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .build()
        );

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("john@gmail.com");
        assertThat(response.getAccessToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Customer");

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
    }

    @Test
    @DisplayName("Should throw exception for wrong credentials")
    void login_WithWrongPassword_ShouldThrowException() {
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() ->
                authService.login(loginRequest)
        )
        .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should throw exception when user not found after auth")
    void login_WithUserNotFound_ShouldThrowException() {
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(null);

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login(loginRequest)
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("User not found");
    }
    @Test
    @DisplayName("Should create admin successfully")
    void createAdmin_WithValidRequest_ShouldReturnAuthResponse() {
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("John@123")).thenReturn("$2a$10$encodedPassword");
        
        User adminUser = User.builder().id(2L).email("john@gmail.com").role(Role.ADMIN).build();
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        
        when(refreshTokenService.createRefreshToken(adminUser)).thenReturn(
            RefreshToken.builder().token("refresh-token-123").user(adminUser).expiresAt(LocalDateTime.now().plusDays(7)).build()
        );
        when(jwtUtil.generateAccessToken(2L, "john@gmail.com", "ADMIN")).thenReturn("mock.jwt.token");
        
        AuthResponse response = authService.createAdmin(registerRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo("ADMIN");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating admin with duplicate email")
    void createAdmin_WithDuplicateEmail_ShouldThrowException() {
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(true);
        assertThatThrownBy(() -> authService.createAdmin(registerRequest))
            .isInstanceOf(RuntimeException.class).hasMessageContaining("Email already registered");
    }

    @Test
    @DisplayName("Should refresh access token successfully")
    void refreshAccessToken_WithValidToken_ShouldReturnNewToken() {
        RefreshToken refreshToken = RefreshToken.builder().token("valid-refresh").user(mockUser).build();
        when(refreshTokenService.validateRefreshToken("valid-refresh")).thenReturn(refreshToken);
        when(jwtUtil.generateAccessToken(1L, "john@gmail.com", "USER")).thenReturn("new.jwt.token");
        
        AuthResponse response = authService.refreshAccessToken("valid-refresh");
        
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new.jwt.token");
    }

    @Test
    @DisplayName("Should logout successfully")
    void logout_WithValidToken_ShouldRevoke() {
        authService.logout("valid-refresh");
        verify(refreshTokenService).revokeRefreshToken("valid-refresh");
    }

    @Test
    @DisplayName("Should validate token and extract email successfully")
    void validateAndExtractEmail_WithValidToken_ShouldReturnEmail() {
        when(jwtUtil.extractEmail("valid-token")).thenReturn("john@gmail.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(mockUser));
        
        String email = authService.validateAndExtractEmail("valid-token");
        assertThat(email).isEqualTo("john@gmail.com");
    }

    @Test
    @DisplayName("Should throw exception if token email is null")
    void validateAndExtractEmail_WithInvalidToken_ShouldThrowException() {
        when(jwtUtil.extractEmail("invalid-token")).thenReturn(null);
        assertThatThrownBy(() -> authService.validateAndExtractEmail("invalid-token"))
            .isInstanceOf(RuntimeException.class).hasMessageContaining("Invalid token");
    }

    @Test
    @DisplayName("Should update user role successfully")
    void updateUserRole_WithValidData_ShouldUpdate() {
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(mockUser));
        authService.updateUserRole("john@gmail.com", "ADMIN");
        verify(userRepository).save(mockUser);
        assertThat(mockUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Should throw exception if invalid role")
    void updateUserRole_WithInvalidRole_ShouldThrowException() {
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(mockUser));
        assertThatThrownBy(() -> authService.updateUserRole("john@gmail.com", "INVALID_ROLE"))
            .isInstanceOf(RuntimeException.class).hasMessageContaining("Invalid role");
    }

    @Test
    @DisplayName("Should get user by email")
    void getUserByEmail_ShouldReturnUserResponse() {
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(mockUser));
        com.InsuranceManagementSystem.AuthService.dtos.UserResponse response = authService.getUserByEmail("john@gmail.com");
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("john@gmail.com");
    }

    @Test
    @DisplayName("Should get all users")
    void getAllUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(java.util.List.of(mockUser));
        java.util.List<com.InsuranceManagementSystem.AuthService.dtos.UserResponse> response = authService.getAllUsers();
        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("Should get users by role")
    void getUsersByRole_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(java.util.List.of(mockUser));
        java.util.List<com.InsuranceManagementSystem.AuthService.dtos.UserResponse> response = authService.getUsersByRole("USER");
        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("Should get total user count")
    void getTotalUserCount_ShouldReturnCount() {
        when(userRepository.count()).thenReturn(5L);
        long count = authService.getTotalUserCount();
        assertThat(count).isEqualTo(5L);
    }
}
