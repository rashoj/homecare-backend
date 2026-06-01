package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FamilyDocumentResponse {

    private Long id;

    private String documentName;
    private String documentType;
    private String fileName;
    private String contentType;

    private LocalDate expirationDate;
    private String approvalStatus;

    private LocalDateTime uploadedAt;
}