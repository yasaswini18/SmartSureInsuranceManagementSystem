package com.InsuranceManagementSystem.ApiGateway;

import com.InsuranceManagementSystem.ApiGateway.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void publicRoute_authLogin_shouldPassFilterAndReturn503() {
        // Will return 503 because the downstream service isn't available, but it shouldn't return 401.
        webTestClient.post().uri("/api/auth/login")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void protectedRoute_withoutToken_shouldReturn401() {
        webTestClient.get().uri("/api/policies/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.message").isEqualTo("Authorization header missing");
    }

    @Test
    void protectedRoute_withInvalidToken_shouldReturn401() {
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        webTestClient.get().uri("/api/policies/1")
                .header("Authorization", "Bearer invalid-token")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.message").isEqualTo("Invalid or expired token");
    }

    @Test
    void protectedRoute_withValidToken_nonAdminTryingToAccessAdminRoute_shouldReturn403() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractEmail(anyString())).thenReturn("user@example.com");
        when(jwtUtil.extractRole(anyString())).thenReturn("USER");

        webTestClient.get().uri("/api/admin/dashboard")
                .header("Authorization", "Bearer valid-token")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.message").isEqualTo("Admin access required");
    }
    
    @Test
    void protectedRoute_withValidToken_adminTryingToAccessAdminRoute_shouldPassFilterAndReturn503() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractEmail(anyString())).thenReturn("admin@example.com");
        when(jwtUtil.extractRole(anyString())).thenReturn("ADMIN");

        webTestClient.get().uri("/api/admin/dashboard")
                .header("Authorization", "Bearer valid-token")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
