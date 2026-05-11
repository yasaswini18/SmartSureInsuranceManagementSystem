package com.InsuranceManagementSystem.AuthService.controller;

import com.InsuranceManagementSystem.AuthService.dtos.ApiResponse;
import com.InsuranceManagementSystem.AuthService.dtos.UserResponse;
import com.InsuranceManagementSystem.AuthService.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing users by administrators.
 * Requires ADMIN role for all endpoints.
 */
@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AuthService authService;

    /**
     * Fetches a list of all users in the system.
     *
     * @return ResponseEntity containing a list of UserResponse objects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("Fetching all users");

        List<UserResponse> users = authService.getAllUsers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users fetched successfully",
                        users
                )
        );
    }

    /**
     * Fetches details of a specific user by their email address.
     *
     * @param email the email address of the user
     * @return ResponseEntity containing the UserResponse object
     */
    @GetMapping("/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @PathVariable String email
    ) {
        log.info("Fetching user by email: {}", email);

        UserResponse user = authService.getUserByEmail(email);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User fetched successfully",
                        user
                )
        );
    }

    /**
     * Fetches a list of users filtered by their role.
     *
     * @param role the role to filter users by (e.g., ADMIN, USER)
     * @return ResponseEntity containing a list of UserResponse objects
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable String role
    ) {
        log.info("Fetching users by role: {}", role);

        List<UserResponse> users = authService.getUsersByRole(role);

        return ResponseEntity.ok(
                ApiResponse.success(
                        role + " users fetched successfully",
                        users
                )
        );
    }

    /**
     * Retrieves the total count of registered users in the system.
     *
     * @return ResponseEntity containing the total user count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTotalUserCount() {
        log.info("Fetching total user count");

        long count = authService.getTotalUserCount();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User count fetched successfully",
                        count
                )
        );
    }
}