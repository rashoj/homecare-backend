package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ClockRecordAdjustmentRequest {

    private LocalDateTime clockInTime;

    private LocalDateTime clockOutTime;

    private String adjustmentReason;

    private Long actorUserId;
}