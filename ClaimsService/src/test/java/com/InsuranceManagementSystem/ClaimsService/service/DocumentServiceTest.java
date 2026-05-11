package com.InsuranceManagementSystem.ClaimsService.service;

import com.InsuranceManagementSystem.ClaimsService.dto.ClaimDocumentResponse;
import com.InsuranceManagementSystem.ClaimsService.entity.Claim;
import com.InsuranceManagementSystem.ClaimsService.entity.ClaimDocument;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimStatus;
import com.InsuranceManagementSystem.ClaimsService.repository.ClaimDocumentRepository;
import com.InsuranceManagementSystem.ClaimsService.repository.ClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimDocumentRepository documentRepository;

    @InjectMocks
    private DocumentService documentService;

    private Claim claim;
    private ClaimDocument claimDocument;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "uploadBasePath", "./target/test-uploads/");

        claim = Claim.builder()
                .id(1L)
                .claimNumber("CLM-001")
                .customerEmail("john@gmail.com")
                .status(ClaimStatus.PENDING)
                .build();

        claimDocument = ClaimDocument.builder()
                .id(10L)
                .claim(claim)
                .fileName("test.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .filePath("./target/test-uploads/CLM-001/test.pdf")
                .build();
    }

    @Test
    @DisplayName("Should upload document successfully")
    void uploadDocument_ShouldReturnResponse() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy content".getBytes());

        when(claimRepository.findByIdAndCustomerEmail(1L, "john@gmail.com")).thenReturn(Optional.of(claim));
        when(documentRepository.countByClaimId(1L)).thenReturn(0L);
        when(documentRepository.existsByClaimIdAndFileName(1L, "test.pdf")).thenReturn(false);
        when(documentRepository.getTotalFileSizeByClaimId(1L)).thenReturn(0L);
        when(documentRepository.save(any())).thenReturn(claimDocument);

        ClaimDocumentResponse response = documentService.uploadDocument(1L, file, "john@gmail.com");

        assertThat(response.getFileName()).isEqualTo("test.pdf");
    }

    @Test
    @DisplayName("Should throw when claim not found")
    void uploadDocument_NotFound_ShouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy content".getBytes());
        when(claimRepository.findByIdAndCustomerEmail(1L, "john@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.uploadDocument(1L, file, "john@gmail.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw when claim status is not pending")
    void uploadDocument_NotPending_ShouldThrow() {
        claim.setStatus(ClaimStatus.APPROVED);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy content".getBytes());
        when(claimRepository.findByIdAndCustomerEmail(1L, "john@gmail.com")).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> documentService.uploadDocument(1L, file, "john@gmail.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw when file is empty")
    void uploadDocument_EmptyFile_ShouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[0]);
        when(claimRepository.findByIdAndCustomerEmail(1L, "john@gmail.com")).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> documentService.uploadDocument(1L, file, "john@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Empty file");
    }

    @Test
    @DisplayName("Should throw when file type invalid")
    void uploadDocument_InvalidType_ShouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "dummy".getBytes());
        when(claimRepository.findByIdAndCustomerEmail(1L, "john@gmail.com")).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> documentService.uploadDocument(1L, file, "john@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid file type");
    }

    @Test
    @DisplayName("Should download document for admin")
    void downloadDocument_Admin_ShouldReturnResource() throws IOException {
        Path testFile = Paths.get("./target/test-uploads/CLM-001/test.pdf");
        Files.createDirectories(testFile.getParent());
        if (!Files.exists(testFile)) {
            Files.createFile(testFile);
        }

        when(documentRepository.findById(10L)).thenReturn(Optional.of(claimDocument));
        Resource resource = documentService.downloadDocument(10L, "admin@gmail.com", "ADMIN");
        assertThat(resource.exists()).isTrue();
    }

    @Test
    @DisplayName("Should throw when downloading for other user")
    void downloadDocument_OtherUser_ShouldThrow() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(claimDocument));
        assertThatThrownBy(() -> documentService.downloadDocument(10L, "other@gmail.com", "USER"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should get documents for claim")
    void getDocumentsForClaim_ShouldReturnList() {
        when(claimRepository.findByIdAndCustomerEmail(1L, "john@gmail.com")).thenReturn(Optional.of(claim));
        when(documentRepository.findByClaimId(1L)).thenReturn(List.of(claimDocument));

        List<ClaimDocumentResponse> responses = documentService.getDocumentsForClaim(1L, "john@gmail.com", "USER");
        assertThat(responses).hasSize(1);
    }
}
