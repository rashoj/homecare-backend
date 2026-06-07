package com.homecare.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EVVAlertResponse {

    private Long id;
    private Long exceptionId;
    private Long clientId;
    private Long caregiverId;
    private Long appointmentId;
    private Long organizationId;

    private String alertType;
    private String severity;
    private String status;
    private String message;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}