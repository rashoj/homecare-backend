package com.homecare.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MessageResponse {

    private Long id;

    private Long conversationId;

    private Long senderUserId;

    private String senderName;

    private String senderRole;

    private String messageBody;

    private Boolean readByRecipient;

    private LocalDateTime sentAt;

    private LocalDateTime readAt;
}