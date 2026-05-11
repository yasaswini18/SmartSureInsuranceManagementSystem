package com.InsuranceManagementSystem.AuthService.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.InsuranceManagementSystem.AuthService.dtos.ApiResponse;
import com.InsuranceManagementSystem.AuthService.dtos.AuthResponse;
import com.InsuranceManagementSystem.AuthService.dtos.LoginRequest;
import com.InsuranceManagementSystem.AuthService.dtos.RegisterRequest;
import com.InsuranceManagementSystem.AuthService.dtos.UpdateUserRoleRequest;
import com.InsuranceManagementSystem.AuthService.security.JwtUtil;
import com.InsuranceManagementSystem.AuthService.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller class for handling authentication related requests.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Authentication",
    description = "Endpoints for user registration, login, admin management and token validation"
)
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @return a ResponseEntity containing the AuthResponse
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Creates a new CUSTOMER account and returns JWT token"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or email already exists"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Logs in a user.
     *
     * @param request the login request containing credentials
     * @param response the HTTP servlet response
     * @return a ResponseEntity containing the AuthResponse
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates user and returns JWT token"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request);
        addRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    /**
     * Refreshes the access token using the refresh token from the cookie.
     *
     * @param refreshToken the refresh token from the cookie
     * @param response the HTTP servlet response
     * @return a ResponseEntity containing the new AuthResponse
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Validates an opaque refresh token and returns a new short-lived access token"
    )
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.refreshAccessToken(refreshToken);
        addRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Logs out the user by revoking the refresh token.
     *
     * @param refreshToken the refresh token from the cookie
     * @param response the HTTP servlet response
     * @return a ResponseEntity indicating success
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Logout",
        description = "Revokes the provided refresh token and logs the user out"
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Creates a new admin user.
     *
     * @param request the registration request for admin
     * @param response the HTTP servlet response
     * @return a ResponseEntity containing the AuthResponse
     */
    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create admin account",
        description = "Creates a new ADMIN account. Requires ADMIN role"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Admin created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AuthResponse> createAdmin(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.createAdmin(request);
        addRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    /**
     * Health check endpoint to verify if the service is running.
     *
     * @return a ResponseEntity with a status message
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Check if Auth Service is running"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.status(HttpStatus.OK).body("Auth Service is running ✅");
    }

    /**
     * Validates a JWT token and returns user details.
     *
     * @param authHeader the authorization header containing the token
     * @return a ResponseEntity containing the user details
     */
    @GetMapping("/validate")
    @Operation(
        summary = "Validate token",
        description = "Validates JWT token and returns user email and role"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> validate(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        String email = authService.validateAndExtractEmail(token);
        String role  = jwtUtil.extractRole(token);

        AuthResponse body = AuthResponse.builder()
                .userId(jwtUtil.extractUserId(token))
                .email(email)
                .role(role)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Token is valid", body));
    }

    /**
     * Updates the role of a user.
     *
     * @param email the email of the user
     * @param request the update request containing the new role
     * @return a ResponseEntity with a success message
     */
    @PatchMapping("/{email}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user role",
        description = "Allows ADMIN to update the role of a user"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User role updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> updateUserRole(
            @PathVariable String email,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        authService.updateUserRole(email, request.getRole());
        return ResponseEntity.ok("User role updated successfully");
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
