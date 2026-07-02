package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class WorkforceReadinessResponse {

    private Long caregiverId;
    private String caregiverName;

    private Long organizationId;

    private Integer readinessScore;
    private Boolean readyToWork;
    private Boolean schedulingBlocked;

    private Integer totalRules;
    private Integer completedRules;
    private Integer missingRules;
    private Integer expiredRules;
    private Integer warningRules;

    private List<String> blockingReasons;
    private List<String> warnings;


    private List<WorkforceReadinessIssue> blockingRequirements;
    private List<WorkforceReadinessIssue> warningRequirements;

    private List<WorkforceRequirementStatus> requirements;

}