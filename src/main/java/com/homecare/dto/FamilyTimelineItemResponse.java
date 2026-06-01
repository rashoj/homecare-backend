package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FamilyTimelineItemResponse {

    private String type;
    private String title;
    private String description;
    private LocalDateTime timestamp;

    private String relatedEntityType;
    private Long relatedEntityId;
}