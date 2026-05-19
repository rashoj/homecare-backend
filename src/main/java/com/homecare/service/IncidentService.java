package com.homecare.service;

import com.homecare.dto.IncidentRequest;
import com.homecare.dto.IncidentResponse;
import com.homecare.dto.IncidentReviewRequest;
import com.homecare.entity.*;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentReviewRepository incidentReviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public IncidentService(
            IncidentRepository incidentRepository,
            IncidentReviewRepository incidentReviewRepository,
            AppointmentRepository appointmentRepository,
            ClientRepository clientRepository,
            UserRepository userRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentReviewRepository = incidentReviewRepository;
        this.appointmentRepository = appointmentRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    public IncidentResponse createIncident(IncidentRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        User caregiver = userRepository.findById(request.getCaregiverId())
                .orElseThrow(() -> new RuntimeException("Caregiver not found"));

        Appointment appointment = null;

        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
        }

        Incident incident = Incident.builder()
                .appointment(appointment)
                .client(client)
                .caregiver(caregiver)
                .incidentDateTime(request.getIncidentDateTime())
                .incidentType(request.getIncidentType())
                .severity(normalizeSeverity(request.getSeverity()))
                .description(request.getDescription())
                .immediateActionTaken(request.getImmediateActionTaken())
                .witnessName(request.getWitnessName())
                .witnessPhone(request.getWitnessPhone())
                .witnessStatement(request.getWitnessStatement())
                .status("SUBMITTED")
                .stateReportable(Boolean.TRUE.equals(request.getStateReportable()))
                .build();

        return mapToResponse(incidentRepository.save(incident));
    }

    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<IncidentResponse> getIncidentsByClient(Long clientId) {
        return incidentRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<IncidentResponse> getIncidentsByCaregiver(Long caregiverId) {
        return incidentRepository.findByCaregiverIdOrderByCreatedAtDesc(caregiverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        return mapToResponse(incident);
    }

    public IncidentResponse reviewIncident(Long incidentId, IncidentReviewRequest request) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        User reviewedBy = null;

        if (request.getReviewedByUserId() != null) {
            reviewedBy = userRepository.findById(request.getReviewedByUserId())
                    .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        }

        IncidentReview review = incidentReviewRepository.findByIncidentId(incidentId)
                .orElse(
                        IncidentReview.builder()
                                .incident(incident)
                                .build()
                );

        String status = normalizeReviewStatus(request.getReviewStatus());

        review.setReviewedBy(reviewedBy);
        review.setReviewStatus(status);
        review.setSupervisorNotes(request.getSupervisorNotes());
        review.setCorrectiveAction(request.getCorrectiveAction());
        review.setFollowUpRequired(request.getFollowUpRequired());

        incident.setStatus(status);

        incidentReviewRepository.save(review);
        incidentRepository.save(incident);

        return mapToResponse(incident);
    }

    private IncidentResponse mapToResponse(Incident incident) {
        IncidentReview review = incidentReviewRepository.findByIncidentId(incident.getId())
                .orElse(null);

        return IncidentResponse.builder()
                .id(incident.getId())
                .appointmentId(incident.getAppointment() != null ? incident.getAppointment().getId() : null)
                .clientId(incident.getClient().getId())
                .clientName(incident.getClient().getFullName())
                .caregiverId(incident.getCaregiver().getId())
                .caregiverName(incident.getCaregiver().getFullName())
                .incidentDateTime(incident.getIncidentDateTime())
                .incidentType(incident.getIncidentType())
                .severity(incident.getSeverity())
                .description(incident.getDescription())
                .immediateActionTaken(incident.getImmediateActionTaken())
                .witnessName(incident.getWitnessName())
                .witnessPhone(incident.getWitnessPhone())
                .witnessStatement(incident.getWitnessStatement())
                .status(incident.getStatus())
                .stateReportable(incident.getStateReportable())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .reviewStatus(review != null ? review.getReviewStatus() : null)
                .supervisorNotes(review != null ? review.getSupervisorNotes() : null)
                .correctiveAction(review != null ? review.getCorrectiveAction() : null)
                .followUpRequired(review != null ? review.getFollowUpRequired() : null)
                .build();
    }

    private String normalizeSeverity(String severity) {
        if (severity == null || severity.isBlank()) {
            return "LOW";
        }

        String value = severity.toUpperCase();

        if (!value.equals("LOW")
                && !value.equals("MEDIUM")
                && !value.equals("HIGH")
                && !value.equals("CRITICAL")) {
            throw new RuntimeException("Severity must be LOW, MEDIUM, HIGH, or CRITICAL.");
        }

        return value;
    }

    private String normalizeReviewStatus(String status) {
        if (status == null || status.isBlank()) {
            return "UNDER_REVIEW";
        }

        String value = status.toUpperCase();

        if (!value.equals("UNDER_REVIEW")
                && !value.equals("RESOLVED")
                && !value.equals("CLOSED")) {
            throw new RuntimeException("Review status must be UNDER_REVIEW, RESOLVED, or CLOSED.");
        }

        return value;
    }
}