package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentReferralReviewRequest {

    private String status;
    // UNDER_REVIEW, APPROVED, REJECTED

    private String adminNotes;

    private Long reviewedByUserId;
}