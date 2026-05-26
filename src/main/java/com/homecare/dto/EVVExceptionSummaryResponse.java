package com.homecare.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EVVExceptionSummaryResponse {

    private long total;
    private long needsReview;
    private long reviewed;
    private long resolved;
    private long highSeverity;
}