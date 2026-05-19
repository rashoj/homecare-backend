package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceDocumentationReviewRequest {

    private String status;

    private String supervisorComments;
}