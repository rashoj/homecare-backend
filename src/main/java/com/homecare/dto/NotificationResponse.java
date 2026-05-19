package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificationResponse {

    private Long id;

    private Long userId;

    private String userName;

    private String title;

    private String message;

    private String type;

    private Boolean isRead;

    private String relatedEntityType;

    private Long relatedEntityId;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}