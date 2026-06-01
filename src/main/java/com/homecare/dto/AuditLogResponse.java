package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AuditLogResponse {

    private Long id;

    private Long actorUserId;
    private String actorName;
    private String actorRole;

    private Long clientId;

    private String action;
    private String resourceType;
    private Long resourceId;

    private String description;
    private String ipAddress;
    private String userAgent;

    private LocalDateTime createdAt;
}