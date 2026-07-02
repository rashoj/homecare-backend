package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class WorkforceReadinessIssue {

    private String recordType;
    private String displayName;

    private String reason;
    // MISSING, EXPIRED, FAILED, EXPIRING_SOON

    private Boolean required;
    private Boolean blockScheduling;

    private LocalDate expirationDate;

    private String message;
}