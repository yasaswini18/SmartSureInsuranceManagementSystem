package com.InsuranceManagementSystem.ClaimsService.controller;

import com.InsuranceManagementSystem.ClaimsService.dto.*;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimStatus;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimType;
import com.InsuranceManagementSystem.ClaimsService.external.PolicyServiceClient;
import com.InsuranceManagementSystem.ClaimsService.security.JwtAuthFilter;
import com.InsuranceManagementSystem.ClaimsService.security.JwtUtil;
import com.InsuranceManagementSystem.ClaimsService.service.ClaimsService;
import com.InsuranceManagementSystem.ClaimsService.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ClaimsController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class ClaimsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimsService claimsService;

    @MockBean
    private PolicyServiceClient policyServiceClient;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private ClaimResponse mockClaimResponse;

    @BeforeEach
    void setupSecurity() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "john@gmail.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockClaimResponse = ClaimResponse.builder()
                .id(1L)
                .claimNumber("CLM-001")
                .policyNumber("POL-001")
                .status(ClaimStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("POST /api/claims/initiate -> 201 Created")
    void initiateClaim_ShouldReturn201() throws Exception {
        InitiateClaimRequest request = new InitiateClaimRequest(
                "POL-001", ClaimType.MEDICAL, "Detailed valid description of the incident", LocalDateTime.now(), new BigDecimal("1000")
        );

        when(claimsService.initiateClaim(any(InitiateClaimRequest.class), anyString(), anyString()))
                .thenReturn(mockClaimResponse);

        mockMvc.perform(post("/api/claims/initiate")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-001"));
    }

    @Test
    @DisplayName("POST /api/claims/{claimId}/documents -> 201 Created")
    void uploadDocument_ShouldReturn201() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test".getBytes());
        ClaimDocumentResponse docResponse = ClaimDocumentResponse.builder().fileName("test.pdf").build();

        when(documentService.uploadDocument(anyLong(), any(), anyString())).thenReturn(docResponse);

        mockMvc.perform(multipart("/api/claims/1/documents")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileName").value("test.pdf"));
    }

    @Test
    @DisplayName("GET /api/claims/my-claims -> 200 OK")
    void getMyClaims_ShouldReturn200() throws Exception {
        ClaimSummaryResponse summary = ClaimSummaryResponse.builder().claimNumber("CLM-001").build();
        when(claimsService.getMyClaims(anyString())).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/claims/my-claims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].claimNumber").value("CLM-001"));
    }

    @Test
    @DisplayName("GET /api/claims/status/{claimId} -> 200 OK")
    void trackClaimStatus_ShouldReturn200() throws Exception {
        when(claimsService.trackClaimStatus(anyLong(), anyString())).thenReturn(mockClaimResponse);

        mockMvc.perform(get("/api/claims/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-001"));
    }

    @Test
    @DisplayName("GET /api/claims/{claimId} -> 200 OK")
    void getClaimById_ShouldReturn200() throws Exception {
        when(claimsService.getClaimById(anyLong(), anyString(), anyString())).thenReturn(mockClaimResponse);

        mockMvc.perform(get("/api/claims/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-001"));
    }

    @Test
    @DisplayName("GET /api/claims/all -> 200 OK")
    void getAllClaims_ShouldReturn200() throws Exception {
        ClaimSummaryResponse summary = ClaimSummaryResponse.builder().claimNumber("CLM-001").build();
        when(claimsService.getAllClaims()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/claims/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].claimNumber").value("CLM-001"));
    }

    @Test
    @DisplayName("GET /api/claims/pending -> 200 OK")
    void getPendingClaims_ShouldReturn200() throws Exception {
        ClaimSummaryResponse summary = ClaimSummaryResponse.builder().claimNumber("CLM-001").build();
        when(claimsService.getPendingClaims()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/claims/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].claimNumber").value("CLM-001"));
    }

    @Test
    @DisplayName("GET /api/claims/{claimId}/documents -> 200 OK")
    void getDocumentsForClaim_ShouldReturn200() throws Exception {
        ClaimDocumentResponse doc = ClaimDocumentResponse.builder().fileName("test.pdf").build();
        when(documentService.getDocumentsForClaim(anyLong(), anyString(), anyString())).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/claims/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fileName").value("test.pdf"));
    }

    @Test
    @DisplayName("GET /api/claims/documents/{documentId}/download -> 200 OK")
    void downloadDocument_ShouldReturn200() throws Exception {
        Resource mockResource = new ByteArrayResource("test data".getBytes()) {
            @Override
            public String getFilename() {
                return "test.pdf";
            }
        };

        when(documentService.downloadDocument(anyLong(), anyString(), anyString())).thenReturn(mockResource);

        mockMvc.perform(get("/api/claims/documents/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.pdf\""));
    }

    @Test
    @DisplayName("PUT /api/claims/{claimId}/start-review -> 200 OK")
    void startReview_ShouldReturn200() throws Exception {
        when(claimsService.startReview(anyLong(), anyString())).thenReturn(mockClaimResponse);

        mockMvc.perform(put("/api/claims/1/start-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-001"));
    }

    @Test
    @DisplayName("PUT /api/claims/{claimId}/review -> 200 OK")
    void reviewClaim_ShouldReturn200() throws Exception {
        ClaimReviewRequest request = new ClaimReviewRequest(ClaimStatus.APPROVED, "Valid remarks string", new BigDecimal("100"));
        when(claimsService.reviewClaim(anyLong(), any(), anyString())).thenReturn(mockClaimResponse);

        mockMvc.perform(put("/api/claims/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-001"));
    }

    @Test
    @DisplayName("PUT /api/claims/{claimId}/settle -> 200 OK")
    void settleClaim_ShouldReturn200() throws Exception {
        when(claimsService.settleClaim(anyLong(), anyString())).thenReturn(mockClaimResponse);

        mockMvc.perform(put("/api/claims/1/settle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-001"));
    }
}