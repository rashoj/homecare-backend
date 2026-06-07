package com.homecare.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformDashboardResponse {
    private long totalDemoRequests;
    private long newDemoRequests;
    private long contactedDemoRequests;
    private long qualifiedDemoRequests;
    private long demoScheduledRequests;
    private long closedDemoRequests;

    private long totalContactRequests;
    private long newContactRequests;
    private long respondedContactRequests;
    private long closedContactRequests;
}