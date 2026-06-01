package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RecentActivityResponse {

    private Long id;
    private String action;
    private String actorName;
    private String resourceType;
    private Long resourceId;
    private String description;
    private LocalDateTime createdAt;
}