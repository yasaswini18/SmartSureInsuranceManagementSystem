package com.InsuranceManagementSystem.AdminService.controller;

import com.InsuranceManagementSystem.AdminService.dto.ApiResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.UserResponse;
import com.InsuranceManagementSystem.AdminService.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for administrative management of system users.
 * Provides endpoints for viewing all users, fetching by email, role,
 * and getting the total user count. All endpoints require the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@io.swagger.v3.oas.annotations.tags.Tag(
        name = "Admin - User Management",
        description = "Admin user viewing and management. All endpoints require an ADMIN token."
)
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * Retrieves all registered users in the system.
     *
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing a list of {@link UserResponse}.
     */
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all users",
            description = "Admin fetches all registered users."
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        List<UserResponse> users =
                adminUserService.getAllUsers(adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", users)
        );
    }

    /**
     * Retrieves detailed information for a specific user by email.
     *
     * @param email       The email of the targeted user.
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the {@link UserResponse}.
     */
    @GetMapping("/{email}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get user by email",
            description = "Admin fetches a specific user by their email address."
    )
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @PathVariable String email,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        UserResponse user =
                adminUserService.getUserByEmail(email, adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("User fetched successfully", user)
        );
    }

    /**
     * Retrieves all users having a specific role.
     *
     * @param role        The role to filter by (e.g., ADMIN, USER).
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing a list of {@link UserResponse}.
     */
    @GetMapping("/role/{role}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get users by role",
            description = "Admin fetches users filtered by a specific role."
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable String role,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        List<UserResponse> users =
                adminUserService.getUsersByRole(role, adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success(role + " users fetched successfully", users)
        );
    }

    /**
     * Retrieves the total count of all registered users.
     *
     * @param bearerToken The administrator's authorization token.
     * @return {@link ResponseEntity} containing the total user count.
     */
    @GetMapping("/count")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get total user count",
            description = "Admin fetches the total number of registered users."
    )
    public ResponseEntity<ApiResponse<Long>> getTotalUserCount(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = getCurrentUserEmail();

        Long count =
                adminUserService.getTotalUserCount(adminEmail, bearerToken);

        return ResponseEntity.ok(
                ApiResponse.success("User count fetched successfully", count)
        );
    }

    /**
     * Helper method to retrieve the currently authenticated admin's email.
     *
     * @return The admin's email address.
     */
    private String getCurrentUserEmail() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}