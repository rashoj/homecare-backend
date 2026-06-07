package com.homecare.service;

import com.homecare.dto.DemoRequestCreateRequest;
import com.homecare.dto.DemoRequestResponse;
import com.homecare.entity.DemoRequest;
import com.homecare.entity.User;
import com.homecare.repository.DemoRequestRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.homecare.entity.User;

import java.util.List;

@Service
public class DemoRequestService {

    private final DemoRequestRepository demoRequestRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public DemoRequestService(
            DemoRequestRepository demoRequestRepository,
            AuditLogService auditLogService,UserRepository userRepository
    ) {
        this.demoRequestRepository = demoRequestRepository;
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    public DemoRequestResponse createDemoRequest(DemoRequestCreateRequest request) {
        DemoRequest demoRequest = DemoRequest.builder()
                .fullName(request.getFullName())
                .agencyName(request.getAgencyName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .agencySize(request.getAgencySize())
                .message(request.getMessage())
                .status("NEW")
                .build();

        DemoRequest saved = demoRequestRepository.save(demoRequest);

        auditLogService.logAction(
                null,
                request.getFullName(),
                "PUBLIC",
                null,
                "DEMO_REQUEST_CREATED",
                "DEMO_REQUEST",
                saved.getId(),
                "Public demo request submitted by "
                        + request.getFullName()
                        + " from "
                        + request.getAgencyName()
        );

        return mapToResponse(saved);
    }

    public List<DemoRequestResponse> getAllDemoRequests() {
        return demoRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DemoRequestResponse updateStatus(
            Long id,
            String status,
            String actorEmail
    ) {
        DemoRequest demoRequest = demoRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demo request not found"));

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new RuntimeException("Actor user not found"));

        String oldStatus = demoRequest.getStatus();

        if (oldStatus != null && oldStatus.equals(status)) {
            return mapToResponse(demoRequest);
        }

        demoRequest.setStatus(status);

        DemoRequest saved = demoRequestRepository.save(demoRequest);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                null,
                "DEMO_REQUEST_STATUS_UPDATED",
                "DEMO_REQUEST",
                saved.getId(),
                "Demo request status updated from "
                        + oldStatus
                        + " to "
                        + status
                        + " by "
                        + actor.getFullName()
        );

        return mapToResponse(saved);
    }
    private DemoRequestResponse mapToResponse(DemoRequest demoRequest) {
        return DemoRequestResponse.builder()
                .id(demoRequest.getId())
                .fullName(demoRequest.getFullName())
                .agencyName(demoRequest.getAgencyName())
                .email(demoRequest.getEmail())
                .phone(demoRequest.getPhone())
                .agencySize(demoRequest.getAgencySize())
                .message(demoRequest.getMessage())
                .status(demoRequest.getStatus())
                .createdAt(demoRequest.getCreatedAt())
                .updatedAt(demoRequest.getUpdatedAt())
                .build();
    }
}