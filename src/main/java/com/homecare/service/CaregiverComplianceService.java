package com.homecare.service;

import com.homecare.dto.CaregiverComplianceRequest;
import com.homecare.dto.CaregiverComplianceResponse;
import com.homecare.entity.CaregiverComplianceRecord;
import com.homecare.entity.User;
import com.homecare.repository.CaregiverComplianceRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CaregiverComplianceService {

    private final CaregiverComplianceRepository repository;
    private final UserRepository userRepository;

    public CaregiverComplianceService(
            CaregiverComplianceRepository repository,
            UserRepository userRepository
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public CaregiverComplianceResponse create(
            CaregiverComplianceRequest request,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        User caregiver = userRepository.findById(request.getCaregiverId())
                .orElseThrow(() -> new RuntimeException("Caregiver not found."));

        if (caregiver.getOrganization() == null ||
                !caregiver.getOrganization().getId().equals(actor.getOrganization().getId())) {
            throw new RuntimeException("Caregiver does not belong to this organization.");
        }

        CaregiverComplianceRecord record = CaregiverComplianceRecord.builder()
                .organization(actor.getOrganization())
                .caregiver(caregiver)
                .recordType(request.getRecordType())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .completedDate(request.getCompletedDate())
                .expirationDate(request.getExpirationDate())
                .notes(request.getNotes())
                .verifiedBy(actor.getId())
                .verifiedAt(LocalDateTime.now())
                .build();

        return mapToResponse(repository.save(record));
    }

    public List<CaregiverComplianceResponse> getByCaregiver(
            Long caregiverId,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver not found."));

        if (caregiver.getOrganization() == null ||
                !caregiver.getOrganization().getId().equals(actor.getOrganization().getId())) {
            throw new RuntimeException("Caregiver does not belong to this organization.");
        }

        return repository.findByCaregiverIdOrderByCreatedAtDesc(caregiverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<CaregiverComplianceResponse> getExpiredOrExpiring(
            String actorEmail,
            int days
    ) {
        User actor = getActor(actorEmail);
        LocalDate cutoff = LocalDate.now().plusDays(days);

        return repository
                .findByOrganizationIdAndExpirationDateBeforeOrderByExpirationDateAsc(
                        actor.getOrganization().getId(),
                        cutoff
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getActor(String actorEmail) {
        User actor = userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        if (actor.getOrganization() == null ||
                actor.getOrganization().getId() == null) {
            throw new RuntimeException("User is not assigned to an organization.");
        }

        return actor;
    }

    private CaregiverComplianceResponse mapToResponse(CaregiverComplianceRecord record) {
        return CaregiverComplianceResponse.builder()
                .id(record.getId())
                .caregiverId(record.getCaregiver().getId())
                .caregiverName(record.getCaregiver().getFullName())
                .organizationId(record.getOrganization().getId())
                .recordType(record.getRecordType())
                .status(record.getStatus())
                .completedDate(record.getCompletedDate())
                .expirationDate(record.getExpirationDate())
                .notes(record.getNotes())
                .verifiedBy(record.getVerifiedBy())
                .verifiedAt(record.getVerifiedAt())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}