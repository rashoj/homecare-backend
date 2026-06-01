package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ConversationResponse {

    private Long id;

    private Long clientId;

    private String subject;

    private String type;

    private Boolean active;

    private Long unreadCount;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private LocalDateTime createdAt;
}