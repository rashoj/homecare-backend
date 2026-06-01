package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLogRequest {

    private Long actorUserId;
    private String actorName;
    private String actorRole;

    private Long clientId;

    private String action;
    private String resourceType;
    private Long resourceId;

    private String description;
}