package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationComplianceRuleRequest {

    private String recordType;
    private String displayName;
    private Boolean required;
    private Integer weight;
    private Boolean blockScheduling;
    private Boolean active;
    private Integer warningDaysBeforeExpiration;
}