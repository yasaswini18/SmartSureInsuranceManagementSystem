package com.InsuranceManagementSystem.ClaimsService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocumentResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private String fileSize;
    private String downloadUrl;
    private LocalDateTime uploadedAt;
}