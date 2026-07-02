package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class WorkforceRequirementStatus {

    private String recordType;
    private String displayName;

    private Boolean required;
    private Boolean blockScheduling;
    private Integer weight;

    private String status;
    private LocalDate completedDate;
    private LocalDate expirationDate;

    private Boolean missing;
    private Boolean expired;
    private Boolean expiringSoon;
    private Boolean blocking;

    private String message;
}