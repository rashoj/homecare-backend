package com.homecare.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ISPGoalProgressResponse {

    private Long id;

    private Long goalId;
    private String goalTitle;

    private Long clientId;
    private String clientName;

    private Long caregiverId;
    private String caregiverName;

    private Long appointmentId;
    private Long serviceDocumentationId;

    private String progressStatus;
    private String promptLevel;
    private String progressNote;

    private LocalDateTime createdAt;
}