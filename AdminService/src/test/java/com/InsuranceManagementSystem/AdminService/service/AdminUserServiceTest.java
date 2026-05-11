package com.InsuranceManagementSystem.AdminService.service;

import com.InsuranceManagementSystem.AdminService.dto.external.ExternalApiResponse;
import com.InsuranceManagementSystem.AdminService.dto.external.UserResponse;
import com.InsuranceManagementSystem.AdminService.enums.AuditAction;
import com.InsuranceManagementSystem.AdminService.external.AuthServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminUserService adminUserService;

    private UserResponse userResponse;
    private final String adminEmail = "admin@insurance.com";
    private final String bearerToken = "Bearer token";

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setFullName("Test User");
        userResponse.setEmail("test@user.com");
        userResponse.setRole("USER");
    }

    private <T> ExternalApiResponse<T> createSuccessResponse(T data) {
        ExternalApiResponse<T> res = new ExternalApiResponse<>();
        res.setSuccess(true);
        res.setMessage("Success");
        res.setData(data);
        return res;
    }

    @Test
    @DisplayName("Should get all users and log success")
    void getAllUsers_ShouldReturnListAndLog() {
        ExternalApiResponse<List<UserResponse>> response = createSuccessResponse(List.of(userResponse));
        when(authServiceClient.getAllUsers(bearerToken)).thenReturn(response);

        List<UserResponse> result = adminUserService.getAllUsers(adminEmail, bearerToken);

        assertThat(result).hasSize(1);
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_ALL_USERS), eq("USER"), eq("ALL"), anyString());
    }

    @Test
    @DisplayName("Should get user by email and log success")
    void getUserByEmail_ShouldReturnUserAndLog() {
        ExternalApiResponse<UserResponse> response = createSuccessResponse(userResponse);
        when(authServiceClient.getUserByEmail("test@user.com", bearerToken)).thenReturn(response);

        UserResponse result = adminUserService.getUserByEmail("test@user.com", adminEmail, bearerToken);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@user.com");
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_USER), eq("USER"), eq("test@user.com"), anyString());
    }

    @Test
    @DisplayName("Should get users by role and log success")
    void getUsersByRole_ShouldReturnListAndLog() {
        ExternalApiResponse<List<UserResponse>> response = createSuccessResponse(List.of(userResponse));
        when(authServiceClient.getUsersByRole("USER", bearerToken)).thenReturn(response);

        List<UserResponse> result = adminUserService.getUsersByRole("USER", adminEmail, bearerToken);

        assertThat(result).hasSize(1);
        verify(auditLogService).logSuccess(eq(adminEmail), eq(AuditAction.VIEW_USERS_BY_ROLE), eq("USER"), eq("USER"), anyString());
    }

    @Test
    @DisplayName("Should return total user count without audit logging")
    void getTotalUserCount_ShouldReturnCount() {
        ExternalApiResponse<Long> response = createSuccessResponse(15L);
        when(authServiceClient.getTotalUserCount(bearerToken)).thenReturn(response);

        Long result = adminUserService.getTotalUserCount(adminEmail, bearerToken);

        assertThat(result).isEqualTo(15L);
        verifyNoInteractions(auditLogService);
    }
}
