package com.InsuranceManagementSystem.AdminService.service;

import com.InsuranceManagementSystem.AdminService.dto.external.UserResponse;
import com.InsuranceManagementSystem.AdminService.enums.AuditAction;
import com.InsuranceManagementSystem.AdminService.external.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing system users from the administrative perspective.
 * Orchestrates communication with the AuthService and records audit logs
 * for all administrative user management actions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final AuthServiceClient authServiceClient;
    private final AuditLogService auditLogService;

    /**
     * Retrieves all users registered in the system.
     *
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return List of {@link UserResponse} representing all users.
     */
    public List<UserResponse> getAllUsers(
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} fetching all users", adminEmail);

        List<UserResponse> users =
                authServiceClient.getAllUsers(bearerToken).getData();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_ALL_USERS,
                "USER",
                "ALL",
                "Viewed all users. Total count: " + users.size()
        );

        return users;
    }

    /**
     * Retrieves detailed information for a specific user by email.
     *
     * @param email       The email of the target user.
     * @param adminEmail  The email of the administrator making the request.
     * @param bearerToken The administrator's authorization token.
     * @return {@link UserResponse} containing user details.
     */
    public UserResponse getUserByEmail(
            String email,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} fetching user: {}", adminEmail, email);

        UserResponse user =
                authServiceClient.getUserByEmail(email, bearerToken).getData();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_USER,
                "USER",
                email,
                "Viewed user profile: " + email
        );

        return user;
    }

    /**
     * Retrieves all users having a specific role (e.g., ADMIN, USER).
     *
     * @param role        The target role.
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return List of {@link UserResponse} matching the role.
     */
    public List<UserResponse> getUsersByRole(
            String role,
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} fetching users by role: {}", adminEmail, role);

        List<UserResponse> users =
                authServiceClient.getUsersByRole(role, bearerToken).getData();

        auditLogService.logSuccess(
                adminEmail,
                AuditAction.VIEW_USERS_BY_ROLE,
                "USER",
                role,
                "Viewed users with role: " + role + ". Count: " + users.size()
        );

        return users;
    }

    /**
     * Retrieves the total count of registered users.
     *
     * @param adminEmail  The email of the administrator.
     * @param bearerToken The administrator's authorization token.
     * @return The total user count.
     */
    public Long getTotalUserCount(
            String adminEmail,
            String bearerToken
    ) {
        log.info("Admin {} fetching user count", adminEmail);

        return authServiceClient.getTotalUserCount(bearerToken).getData();
    }
}