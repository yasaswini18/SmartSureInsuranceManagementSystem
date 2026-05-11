package com.InsuranceManagementSystem.ClaimsService.service;

import com.InsuranceManagementSystem.ClaimsService.dto.ClaimReviewRequest;
import com.InsuranceManagementSystem.ClaimsService.dto.ClaimResponse;
import com.InsuranceManagementSystem.ClaimsService.dto.ClaimSummaryResponse;
import com.InsuranceManagementSystem.ClaimsService.dto.InitiateClaimRequest;
import com.InsuranceManagementSystem.ClaimsService.dto.PolicyValidationResponse;
import com.InsuranceManagementSystem.ClaimsService.entity.Claim;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimStatus;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimType;
import com.InsuranceManagementSystem.ClaimsService.external.PolicyServiceClient;
import com.InsuranceManagementSystem.ClaimsService.repository.ClaimDocumentRepository;
import com.InsuranceManagementSystem.ClaimsService.repository.ClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimsServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimDocumentRepository documentRepository;

    @Mock
    private PolicyServiceClient policyServiceClient;

    @Mock
    private DocumentService documentService;
    
    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ClaimsService claimsService;

    private Claim mockClaim;
    private PolicyValidationResponse validPolicyResponse;
    private InitiateClaimRequest initiateRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                claimsService, "claimNumberPrefix", "CLM"
        );

        validPolicyResponse = new PolicyValidationResponse(
                true,
                "INS-2024-000001",
                "john@gmail.com",
                "Health Shield Basic",
                "HEALTH",
                new BigDecimal("500000.00"),
                LocalDate.now().minusDays(14),
                "ACTIVE",
                null
        );

        initiateRequest = new InitiateClaimRequest(
                "INS-2024-000001",
                ClaimType.MEDICAL,
                "Hospitalized for 3 days due to fever treatment",
                LocalDateTime.now().minusDays(5),
                new BigDecimal("45000.00")
        );

        mockClaim = Claim.builder()
                .id(1L)
                .claimNumber("CLM-2024-000001")
                .policyNumber("INS-2024-000001")
                .customerEmail("john@gmail.com")
                .claimType(ClaimType.MEDICAL)
                .description("Hospitalized for 3 days")
                .incidentDate(LocalDateTime.now().minusDays(5))
                .claimedAmount(new BigDecimal("45000.00"))
                .status(ClaimStatus.PENDING)
                .productName("Health Shield Basic")
                .policyType("HEALTH")
                .coverageAmount(new BigDecimal("500000.00"))
                .documents(new ArrayList<>())
                .build();

        mockClaim.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should initiate claim successfully")
    void initiateClaim_WithValidRequest_ShouldReturnResponse() {
        when(policyServiceClient.validatePolicy(
                anyString(), anyString(), anyString()
        )).thenReturn(validPolicyResponse);

        when(claimRepository.existsByPolicyNumberAndStatusNotIn(
                anyString(), anyList()
        )).thenReturn(false);

        when(claimRepository.count()).thenReturn(0L);

        when(claimRepository.save(any(Claim.class)))
                .thenReturn(mockClaim);

        ClaimResponse response = claimsService.initiateClaim(
                initiateRequest,
                "john@gmail.com",
                "Bearer mock.token"
        );

        assertThat(response).isNotNull();
        assertThat(response.getClaimNumber())
                .isEqualTo("CLM-2024-000001");
        assertThat(response.getStatus())
                .isEqualTo(ClaimStatus.PENDING);
        assertThat(response.getClaimedAmount())
                .isEqualByComparingTo(new BigDecimal("45000.00"));

        verify(claimRepository).save(any(Claim.class));
    }

    @Test
    @DisplayName("Should throw when policy validation fails")
    void initiateClaim_WithInvalidPolicy_ShouldThrowException() {
        PolicyValidationResponse invalidResponse =
                new PolicyValidationResponse(
                        false, null, null, null, null,
                        null, null, null, "Policy not found"
                );

        when(policyServiceClient.validatePolicy(
                anyString(), anyString(), anyString()
        )).thenReturn(invalidResponse);

        assertThatThrownBy(() ->
                claimsService.initiateClaim(
                        initiateRequest,
                        "john@gmail.com",
                        "Bearer mock.token"
                )
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Policy not found");

        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when waiting period is not completed")
    void initiateClaim_WithinWaitingPeriod_ShouldThrow() {
        validPolicyResponse.setStartDate(LocalDate.now().minusDays(3));

        when(policyServiceClient.validatePolicy(
                anyString(), anyString(), anyString()
        )).thenReturn(validPolicyResponse);

        assertThatThrownBy(() ->
                claimsService.initiateClaim(
                        initiateRequest,
                        "john@gmail.com",
                        "Bearer mock.token"
                )
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("after 7 days");
    }

    @Test
    @DisplayName("Should throw when claimed amount exceeds coverage")
    void initiateClaim_WithAmountExceedingCoverage_ShouldThrow() {
        when(policyServiceClient.validatePolicy(
                anyString(), anyString(), anyString()
        )).thenReturn(validPolicyResponse);

        initiateRequest.setClaimedAmount(
                new BigDecimal("999999.00")
        );

        assertThatThrownBy(() ->
                claimsService.initiateClaim(
                        initiateRequest,
                        "john@gmail.com",
                        "Bearer mock.token"
                )
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("Should throw when active claim already exists")
    void initiateClaim_WithExistingActiveClaim_ShouldThrow() {
        when(policyServiceClient.validatePolicy(
                anyString(), anyString(), anyString()
        )).thenReturn(validPolicyResponse);

        when(claimRepository.existsByPolicyNumberAndStatusNotIn(
                anyString(), anyList()
        )).thenReturn(true);

        assertThatThrownBy(() ->
                claimsService.initiateClaim(
                        initiateRequest,
                        "john@gmail.com",
                        "Bearer mock.token"
                )
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Active claim already exists");
    }

    @Test
    @DisplayName("Should throw when incident date is in the future")
    void initiateClaim_WithFutureIncidentDate_ShouldThrow() {
        when(policyServiceClient.validatePolicy(
                anyString(), anyString(), anyString()
        )).thenReturn(validPolicyResponse);

        initiateRequest.setIncidentDate(
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() ->
                claimsService.initiateClaim(
                        initiateRequest,
                        "john@gmail.com",
                        "Bearer mock.token"
                )
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("cannot be in the future");
    }

    @Test
    @DisplayName("Should throw for wrong claim type vs policy type")
    void initiateClaim_WithWrongClaimType_ShouldThrow() {
        when(policyServiceClient.validatePolicy(
                anyString(), anyString(), anyString()
        )).thenReturn(validPolicyResponse);

        when(claimRepository.existsByPolicyNumberAndStatusNotIn(
                anyString(), anyList()
        )).thenReturn(false);

        initiateRequest.setClaimType(ClaimType.THEFT);

        assertThatThrownBy(() ->
                claimsService.initiateClaim(
                        initiateRequest,
                        "john@gmail.com",
                        "Bearer mock.token"
                )
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Invalid claim type");
    }

    @Test
    @DisplayName("Should approve claim successfully")
    void reviewClaim_WithApproval_ShouldUpdateStatus() {
        ClaimReviewRequest reviewRequest =
                new ClaimReviewRequest(
                        ClaimStatus.APPROVED,
                        "All documents verified",
                        new BigDecimal("45000.00")
                );

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(mockClaim));
        when(claimRepository.save(any(Claim.class)))
                .thenReturn(mockClaim);

        ClaimResponse response = claimsService.reviewClaim(
                1L, reviewRequest, "admin@insurance.com"
        );

        verify(claimRepository).save(
                argThat(c ->
                        c.getStatus() == ClaimStatus.APPROVED &&
                        c.getApprovedAmount().compareTo(
                                new BigDecimal("45000.00")
                        ) == 0
                )
        );
    }

    @Test
    @DisplayName("Should reject claim successfully")
    void reviewClaim_WithRejection_ShouldUpdateStatus() {
        ClaimReviewRequest reviewRequest =
                new ClaimReviewRequest(
                        ClaimStatus.REJECTED,
                        "Insufficient documentation provided",
                        null
                );

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(mockClaim));
        when(claimRepository.save(any(Claim.class)))
                .thenReturn(mockClaim);

        claimsService.reviewClaim(
                1L, reviewRequest, "admin@insurance.com"
        );

        verify(claimRepository).save(
                argThat(c ->
                        c.getStatus() == ClaimStatus.REJECTED &&
                        c.getAdminRemarks().equals(
                                "Insufficient documentation provided"
                        )
                )
        );
    }

    @Test
    @DisplayName("Should throw when approving without amount")
    void reviewClaim_ApproveWithoutAmount_ShouldThrow() {
        ClaimReviewRequest reviewRequest =
                new ClaimReviewRequest(
                        ClaimStatus.APPROVED,
                        "Approved",
                        null
                );

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(mockClaim));

        assertThatThrownBy(() ->
                claimsService.reviewClaim(
                        1L, reviewRequest,
                        "admin@insurance.com"
                )
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Invalid approved amount");
    }

    @Test
    @DisplayName("Should throw when claim not found")
    void reviewClaim_WithInvalidId_ShouldThrow() {
        when(claimRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                claimsService.reviewClaim(
                        999L,
                        new ClaimReviewRequest(
                                ClaimStatus.APPROVED,
                                "remarks",
                                new BigDecimal("1000")
                        ),
                        "admin@insurance.com"
                )
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should return my claims")
    void getMyClaims_ShouldReturnList() {
        when(claimRepository.findByCustomerEmail("john@gmail.com")).thenReturn(List.of(mockClaim));
        List<ClaimSummaryResponse> responses = claimsService.getMyClaims("john@gmail.com");
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should track claim status")
    void trackClaimStatus_ShouldReturnResponse() {
        when(claimRepository.findByIdAndCustomerEmail(1L, "john@gmail.com")).thenReturn(Optional.of(mockClaim));
        ClaimResponse response = claimsService.trackClaimStatus(1L, "john@gmail.com");
        assertThat(response.getClaimNumber()).isEqualTo("CLM-2024-000001");
    }

    @Test
    @DisplayName("Should get all claims")
    void getAllClaims_ShouldReturnAll() {
        when(claimRepository.findAll()).thenReturn(List.of(mockClaim));
        List<ClaimSummaryResponse> responses = claimsService.getAllClaims();
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should get pending claims")
    void getPendingClaims_ShouldReturnPending() {
        when(claimRepository.findByStatus(ClaimStatus.PENDING)).thenReturn(List.of(mockClaim));
        List<ClaimSummaryResponse> responses = claimsService.getPendingClaims();
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should get claim by ID for admin")
    void getClaimById_Admin_ShouldReturnResponse() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(mockClaim));
        ClaimResponse response = claimsService.getClaimById(1L, "admin@gmail.com", "ADMIN");
        assertThat(response.getClaimNumber()).isEqualTo("CLM-2024-000001");
    }

    @Test
    @DisplayName("Should get claim by ID for user")
    void getClaimById_User_ShouldReturnResponse() {
        when(claimRepository.findByIdAndCustomerEmail(1L, "john@gmail.com")).thenReturn(Optional.of(mockClaim));
        ClaimResponse response = claimsService.getClaimById(1L, "john@gmail.com", "USER");
        assertThat(response.getClaimNumber()).isEqualTo("CLM-2024-000001");
    }

    @Test
    @DisplayName("Should start review successfully")
    void startReview_ShouldUpdateStatus() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(mockClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(mockClaim);
        claimsService.startReview(1L, "admin@gmail.com");
        verify(claimRepository).save(argThat(c -> c.getStatus() == ClaimStatus.UNDER_REVIEW));
    }

    @Test
    @DisplayName("Should throw when starting review for non-pending")
    void startReview_NotPending_ShouldThrow() {
        mockClaim.setStatus(ClaimStatus.APPROVED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(mockClaim));
        assertThatThrownBy(() -> claimsService.startReview(1L, "admin@gmail.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should settle claim successfully")
    void settleClaim_ShouldUpdateStatus() {
        mockClaim.setStatus(ClaimStatus.APPROVED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(mockClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(mockClaim);
        claimsService.settleClaim(1L, "admin@gmail.com");
        verify(claimRepository).save(argThat(c -> c.getStatus() == ClaimStatus.SETTLED));
    }

    @Test
    @DisplayName("Should throw when settling non-approved")
    void settleClaim_NotApproved_ShouldThrow() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(mockClaim));
        assertThatThrownBy(() -> claimsService.settleClaim(1L, "admin@gmail.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw when getting claim by ID and not found")
    void getClaimById_NotFound_ShouldThrow() {
        when(claimRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> claimsService.getClaimById(1L, "admin@gmail.com", "ADMIN"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should map claim to response correctly")
    void mapToClaimResponse_ShouldMapCorrectly() {
        ClaimResponse response = claimsService.mapToClaimResponse(mockClaim);
        assertThat(response.getClaimNumber()).isEqualTo(mockClaim.getClaimNumber());
    }
    
    @Test
    @DisplayName("Should throw when incident date is more than 1 year in the past")
    void initiateClaim_MoreThanOneYear_ShouldThrow() {
        validPolicyResponse.setStartDate(LocalDate.now().minusYears(3));
        when(policyServiceClient.validatePolicy(anyString(), anyString(), anyString())).thenReturn(validPolicyResponse);
        initiateRequest.setIncidentDate(LocalDateTime.now().minusYears(2));
        assertThatThrownBy(() -> claimsService.initiateClaim(initiateRequest, "john@gmail.com", "Bearer mock.token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("1 year in the past");
    }

    @Test
    @DisplayName("Should throw when user unauthorized or claim not found")
    void getClaimById_UserNotFound_ShouldThrow() {
        when(claimRepository.findByIdAndCustomerEmail(1L, "john@gmail.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> claimsService.getClaimById(1L, "john@gmail.com", "USER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    @DisplayName("Should throw when review claim has invalid state")
    void reviewClaim_InvalidState_ShouldThrow() {
        mockClaim.setStatus(ClaimStatus.SETTLED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(mockClaim));
        ClaimReviewRequest req = new ClaimReviewRequest(ClaimStatus.APPROVED, "Ok", new BigDecimal("100"));
        assertThatThrownBy(() -> claimsService.reviewClaim(1L, req, "admin@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid claim state");
    }

    @Test
    @DisplayName("Should throw when review claim decision is invalid")
    void reviewClaim_InvalidDecision_ShouldThrow() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(mockClaim));
        ClaimReviewRequest req = new ClaimReviewRequest(ClaimStatus.PENDING, "Ok", new BigDecimal("100"));
        assertThatThrownBy(() -> claimsService.reviewClaim(1L, req, "admin@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid decision");
    }

    @Test
    @DisplayName("Should throw when review claim amount exceeds coverage")
    void reviewClaim_ExceedsCoverage_ShouldThrow() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(mockClaim));
        ClaimReviewRequest req = new ClaimReviewRequest(ClaimStatus.APPROVED, "Ok", new BigDecimal("99999999"));
        assertThatThrownBy(() -> claimsService.reviewClaim(1L, req, "admin@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Exceeds coverage amount");
    }

    @Test
    @DisplayName("Should test file size format in mapToClaimResponse")
    void mapToClaimResponse_WithDocuments_ShouldMapCorrectly() {
        com.InsuranceManagementSystem.ClaimsService.entity.ClaimDocument doc1 = com.InsuranceManagementSystem.ClaimsService.entity.ClaimDocument.builder()
                .id(1L).fileName("f1").fileType("pdf").fileSize(500L).uploadedAt(LocalDateTime.now()).build();
        com.InsuranceManagementSystem.ClaimsService.entity.ClaimDocument doc2 = com.InsuranceManagementSystem.ClaimsService.entity.ClaimDocument.builder()
                .id(2L).fileName("f2").fileType("pdf").fileSize(500000L).uploadedAt(LocalDateTime.now()).build();
        com.InsuranceManagementSystem.ClaimsService.entity.ClaimDocument doc3 = com.InsuranceManagementSystem.ClaimsService.entity.ClaimDocument.builder()
                .id(3L).fileName("f3").fileType("pdf").fileSize(5000000L).uploadedAt(LocalDateTime.now()).build();
        
        mockClaim.setDocuments(List.of(doc1, doc2, doc3));
        
        ClaimResponse response = claimsService.mapToClaimResponse(mockClaim);
        assertThat(response.getDocuments()).hasSize(3);
        assertThat(response.getDocuments().get(0).getFileSize()).isEqualTo("500 B");
        assertThat(response.getDocuments().get(1).getFileSize()).contains("KB");
        assertThat(response.getDocuments().get(2).getFileSize()).contains("MB");
    }
}
