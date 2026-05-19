package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class IncidentAttachmentResponse {

    private Long id;

    private Long incidentId;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private LocalDateTime uploadedAt;
}