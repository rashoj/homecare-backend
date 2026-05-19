package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {

    private Long userId;

    private String title;

    private String message;

    private String type;

    private String relatedEntityType;

    private Long relatedEntityId;
}