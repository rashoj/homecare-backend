package com.homecare.service;

import com.homecare.dto.CaregiverTimeEntryRequest;
import com.homecare.dto.CaregiverTimeEntryResponse;
import com.homecare.entity.CaregiverTimeEntry;
import com.homecare.entity.User;
import com.homecare.repository.CaregiverTimeEntryRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CaregiverTimeEntryService {

    private final CaregiverTimeEntryRepository caregiverTimeEntryRepository;
    private final UserRepository userRepository;

    public CaregiverTimeEntryService(
            CaregiverTimeEntryRepository caregiverTimeEntryRepository,
            UserRepository userRepository
    ) {
        this.caregiverTimeEntryRepository = caregiverTimeEntryRepository;
        this.userRepository = userRepository;
    }

    public CaregiverTimeEntryResponse clockIn(
            CaregiverTimeEntryRequest request,
            String actorEmail
    ) {
        User caregiver = getActor(actorEmail);

        if (!"CAREGIVER".equalsIgnoreCase(caregiver.getRole().name())) {
            throw new RuntimeException("Only caregivers can clock in.");
        }

        if (caregiver.getOrganization() == null ||
                caregiver.getOrganization().getId() == null) {
            throw new RuntimeException("Caregiver is not assigned to an organization.");
        }

        caregiverTimeEntryRepository
                .findByCaregiverIdAndStatus(caregiver.getId(), "CLOCKED_IN")
                .ifPresent(entry -> {
                    throw new RuntimeException("Caregiver is already clocked in.");
                });

        CaregiverTimeEntry entry = CaregiverTimeEntry.builder()
                .organization(caregiver.getOrganization())
                .caregiver(caregiver)
                .clockInTime(LocalDateTime.now())
                .clockInLatitude(request.getLatitude())
                .clockInLongitude(request.getLongitude())
                .clockInNotes(request.getNotes())
                .shiftType(
                        request.getShiftType() != null && !request.getShiftType().isBlank()
                                ? request.getShiftType()
                                : "REGULAR_SHIFT"
                )
                .status("CLOCKED_IN")
                .build();

        return mapToResponse(caregiverTimeEntryRepository.save(entry));
    }

    public CaregiverTimeEntryResponse clockOut(
            CaregiverTimeEntryRequest request,
            String actorEmail
    ) {
        User caregiver = getActor(actorEmail);

        CaregiverTimeEntry entry = caregiverTimeEntryRepository
                .findByCaregiverIdAndStatus(caregiver.getId(), "CLOCKED_IN")
                .orElseThrow(() -> new RuntimeException("No active caregiver time entry found."));

        entry.setClockOutTime(LocalDateTime.now());
        entry.setClockOutLatitude(request.getLatitude());
        entry.setClockOutLongitude(request.getLongitude());
        entry.setClockOutNotes(request.getNotes());
        entry.setStatus("CLOCKED_OUT");

        return mapToResponse(caregiverTimeEntryRepository.save(entry));
    }

    public CaregiverTimeEntryResponse getCurrentStatus(String actorEmail) {
        User caregiver = getActor(actorEmail);

        return caregiverTimeEntryRepository
                .findByCaregiverIdAndStatus(caregiver.getId(), "CLOCKED_IN")
                .map(this::mapToResponse)
                .orElse(null);
    }

    public List<CaregiverTimeEntryResponse> getMyTimeEntries(String actorEmail) {
        User caregiver = getActor(actorEmail);

        return caregiverTimeEntryRepository
                .findByCaregiverIdOrderByCreatedAtDesc(caregiver.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<CaregiverTimeEntryResponse> getOrganizationTimeEntries(String actorEmail) {
        User actor = getActor(actorEmail);

        if (actor.getOrganization() == null ||
                actor.getOrganization().getId() == null) {
            throw new RuntimeException("User is not assigned to an organization.");
        }

        return caregiverTimeEntryRepository
                .findByOrganizationIdOrderByCreatedAtDesc(actor.getOrganization().getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getActor(String actorEmail) {
        return userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));
    }

    private CaregiverTimeEntryResponse mapToResponse(CaregiverTimeEntry entry) {
        return CaregiverTimeEntryResponse.builder()
                .id(entry.getId())
                .caregiverId(entry.getCaregiver().getId())
                .caregiverName(entry.getCaregiver().getFullName())
                .organizationId(entry.getOrganization().getId())
                .clockInTime(entry.getClockInTime())
                .clockOutTime(entry.getClockOutTime())
                .totalHours(entry.getTotalHours())
                .shiftType(entry.getShiftType())
                .status(entry.getStatus())
                .clockInNotes(entry.getClockInNotes())
                .clockOutNotes(entry.getClockOutNotes())
                .build();
    }
}