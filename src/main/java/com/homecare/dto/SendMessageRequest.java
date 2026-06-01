package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {

    private Long conversationId;

    private Long senderUserId;

    private String messageBody;
}