package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrganizationComplianceRuleResponse {

    private Long id;
    private Long organizationId;

    private String recordType;
    private String displayName;

    private Boolean required;
    private Integer weight;
    private Boolean blockScheduling;
    private Boolean active;
    private Integer warningDaysBeforeExpiration;
}