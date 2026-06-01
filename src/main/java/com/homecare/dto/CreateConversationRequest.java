package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateConversationRequest {

    private Long clientId;

    private String subject;

    private String type;

    private Long createdByUserId;
}