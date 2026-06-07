package com.homecare.service;

import com.homecare.dto.PlatformDashboardResponse;
import com.homecare.repository.ContactRequestRepository;
import com.homecare.repository.DemoRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class PlatformDashboardService {

    private final DemoRequestRepository demoRequestRepository;
    private final ContactRequestRepository contactRequestRepository;

    public PlatformDashboardService(
            DemoRequestRepository demoRequestRepository,
            ContactRequestRepository contactRequestRepository
    ) {
        this.demoRequestRepository = demoRequestRepository;
        this.contactRequestRepository = contactRequestRepository;
    }

    public PlatformDashboardResponse getDashboard() {
        return PlatformDashboardResponse.builder()
                .totalDemoRequests(demoRequestRepository.count())
                .newDemoRequests(demoRequestRepository.countByStatus("NEW"))
                .contactedDemoRequests(demoRequestRepository.countByStatus("CONTACTED"))
                .qualifiedDemoRequests(demoRequestRepository.countByStatus("QUALIFIED"))
                .demoScheduledRequests(demoRequestRepository.countByStatus("DEMO_SCHEDULED"))
                .closedDemoRequests(demoRequestRepository.countByStatus("CLOSED"))

                .totalContactRequests(contactRequestRepository.count())
                .newContactRequests(contactRequestRepository.countByStatus("NEW"))
                .respondedContactRequests(contactRequestRepository.countByStatus("RESPONDED"))
                .closedContactRequests(contactRequestRepository.countByStatus("CLOSED"))
                .build();
    }
}