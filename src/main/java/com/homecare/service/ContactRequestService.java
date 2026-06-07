package com.homecare.service;

import com.homecare.dto.ContactRequestCreateRequest;
import com.homecare.dto.ContactRequestResponse;
import com.homecare.entity.ContactRequest;
import com.homecare.entity.User;
import com.homecare.repository.ContactRequestRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactRequestService {

    private final ContactRequestRepository contactRequestRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public ContactRequestService(
            ContactRequestRepository contactRequestRepository,
            AuditLogService auditLogService,
            UserRepository userRepository
    ) {
        this.contactRequestRepository = contactRequestRepository;
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    public ContactRequestResponse createContactRequest(
            ContactRequestCreateRequest request
    ) {
        ContactRequest contactRequest = ContactRequest.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status("NEW")
                .build();

        ContactRequest saved = contactRequestRepository.save(contactRequest);

        auditLogService.logAction(
                null,
                request.getFullName(),
                "PUBLIC",
                null,
                "CONTACT_REQUEST_CREATED",
                "CONTACT_REQUEST",
                saved.getId(),
                "Public contact request submitted by "
                        + request.getFullName()
                        + " with subject: "
                        + request.getSubject()
        );

        return mapToResponse(saved);
    }

    public List<ContactRequestResponse> getAllContactRequests() {
        return contactRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ContactRequestResponse updateStatus(
            Long id,
            String status,
            String actorEmail
    ) {
        ContactRequest contactRequest = contactRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact request not found"));

        String oldStatus = contactRequest.getStatus();

        if (oldStatus != null && oldStatus.equals(status)) {
            return mapToResponse(contactRequest);
        }

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new RuntimeException("Actor user not found"));

        contactRequest.setStatus(status);

        ContactRequest saved = contactRequestRepository.save(contactRequest);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                null,
                "CONTACT_REQUEST_STATUS_UPDATED",
                "CONTACT_REQUEST",
                saved.getId(),
                "Contact request status updated from "
                        + oldStatus
                        + " to "
                        + status
                        + " by "
                        + actor.getFullName()
        );

        return mapToResponse(saved);
    }

    private ContactRequestResponse mapToResponse(ContactRequest contactRequest) {
        return ContactRequestResponse.builder()
                .id(contactRequest.getId())
                .fullName(contactRequest.getFullName())
                .email(contactRequest.getEmail())
                .subject(contactRequest.getSubject())
                .message(contactRequest.getMessage())
                .status(contactRequest.getStatus())
                .createdAt(contactRequest.getCreatedAt())
                .updatedAt(contactRequest.getUpdatedAt())
                .build();
    }
}