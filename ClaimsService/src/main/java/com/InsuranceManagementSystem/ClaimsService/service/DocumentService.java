package com.InsuranceManagementSystem.ClaimsService.service;

import com.InsuranceManagementSystem.ClaimsService.dto.ClaimDocumentResponse;
import com.InsuranceManagementSystem.ClaimsService.entity.Claim;
import com.InsuranceManagementSystem.ClaimsService.entity.ClaimDocument;
import com.InsuranceManagementSystem.ClaimsService.enums.ClaimStatus;
import com.InsuranceManagementSystem.ClaimsService.repository.ClaimDocumentRepository;
import com.InsuranceManagementSystem.ClaimsService.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for handling document uploads, downloads, and retrieval for claims.
 * Ensures security checks and file validations are applied.
 */
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository documentRepository;

    @Value("${document.upload.path}")
    private String uploadBasePath;

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/jpg", "image/png"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final long MAX_TOTAL_SIZE = 50 * 1024 * 1024;
    private static final int MAX_DOCUMENTS = 5;

    /**
     * Uploads a document for a specific claim.
     *
     * @param claimId       The ID of the claim.
     * @param file          The file to be uploaded.
     * @param customerEmail The email of the customer uploading the document.
     * @return ClaimDocumentResponse containing document details.
     * @throws IOException If an I/O error occurs during upload.
     */
    @Transactional
    public ClaimDocumentResponse uploadDocument(
            Long claimId,
            MultipartFile file,
            String customerEmail
    ) throws IOException {

        Claim claim = claimRepository
                .findByIdAndCustomerEmail(claimId, customerEmail)
                .orElseThrow(() -> new RuntimeException("Unauthorized"));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new RuntimeException("Cannot upload in current status");
        }

        validateFile(file, claimId);

        Path folder = Paths.get(uploadBasePath + claim.getClaimNumber());
        Files.createDirectories(folder);

        String original = file.getOriginalFilename();
        String unique = UUID.randomUUID().toString().substring(0, 8) + "-" + original;

        Path path = folder.resolve(unique);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        ClaimDocument doc = ClaimDocument.builder()
                .claim(claim)
                .fileName(original)
                .filePath(path.toString())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        ClaimDocument saved = documentRepository.save(doc);

        return ClaimDocumentResponse.builder()
                .id(saved.getId())
                .fileName(saved.getFileName())
                .fileType(saved.getFileType())
                .fileSize(formatFileSize(saved.getFileSize()))
                .downloadUrl("/api/claims/documents/" + saved.getId() + "/download")
                .uploadedAt(saved.getUploadedAt())
                .build();
    }

    /**
     * Downloads a document by its ID.
     *
     * @param documentId The ID of the document to download.
     * @param email      The email of the user requesting the download.
     * @param role       The role of the user.
     * @return Resource representing the file.
     * @throws MalformedURLException If the file path is invalid.
     */
    public Resource downloadDocument(Long documentId, String email, String role)
            throws MalformedURLException {

        ClaimDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (!role.equals("ADMIN") &&
            !doc.getClaim().getCustomerEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        Path path = Paths.get(doc.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not readable");
        }

        return resource;
    }

    /**
     * Retrieves all documents associated with a specific claim.
     *
     * @param claimId The ID of the claim.
     * @param email   The email of the user requesting the documents.
     * @param role    The role of the user.
     * @return List of ClaimDocumentResponse objects.
     */
    public List<ClaimDocumentResponse> getDocumentsForClaim(
            Long claimId, String email, String role) {

        if (!role.equals("ADMIN")) {
            claimRepository.findByIdAndCustomerEmail(claimId, email)
                    .orElseThrow(() -> new RuntimeException("Unauthorized"));
        }

        return documentRepository.findByClaimId(claimId)
                .stream()
                .map(doc -> ClaimDocumentResponse.builder()
                        .id(doc.getId())
                        .fileName(doc.getFileName())
                        .fileType(doc.getFileType())
                        .fileSize(formatFileSize(doc.getFileSize()))
                        .downloadUrl("/api/claims/documents/" + doc.getId() + "/download")
                        .uploadedAt(doc.getUploadedAt())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private void validateFile(MultipartFile file, Long claimId) throws IOException {

        if (file.isEmpty()) throw new RuntimeException("Empty file");

        String type = file.getContentType();
        if (type == null || !ALLOWED_FILE_TYPES.contains(type)) {
            throw new RuntimeException("Invalid file type");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File too large");
        }

        if (documentRepository.countByClaimId(claimId) >= MAX_DOCUMENTS) {
            throw new RuntimeException("Max documents reached");
        }

        if (documentRepository.existsByClaimIdAndFileName(
                claimId, file.getOriginalFilename())) {
            throw new RuntimeException("Duplicate file");
        }

        Long total = documentRepository.getTotalFileSizeByClaimId(claimId);
        if (total + file.getSize() > MAX_TOTAL_SIZE) {
            throw new RuntimeException("Total size exceeded");
        }
    }

    private String formatFileSize(Long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}