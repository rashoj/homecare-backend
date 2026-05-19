package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DocumentResponse {

    private Long id;
    private String documentName;
    private String documentType;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private LocalDate expirationDate;
    private String approvalStatus;
    private String rejectionReason;
    private Long uploadedByUserId;
    private String uploadedByName;
    private Long clientId;
    private String clientName;
    private LocalDateTime uploadedAt;
}