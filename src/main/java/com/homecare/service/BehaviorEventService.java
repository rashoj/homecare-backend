package com.homecare.service;

import com.homecare.dto.BehaviorEventRequest;
import com.homecare.dto.BehaviorEventResponse;
import com.homecare.entity.*;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;
import com.homecare.dto.BehaviorOptionResponse;

import java.util.Arrays;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BehaviorEventService {

    private final BehaviorEventRepository behaviorEventRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceDocumentationRepository serviceDocumentationRepository;

    public BehaviorEventService(
            BehaviorEventRepository behaviorEventRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            ServiceDocumentationRepository serviceDocumentationRepository
    ) {
        this.behaviorEventRepository = behaviorEventRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.serviceDocumentationRepository = serviceDocumentationRepository;
    }

    public BehaviorEventResponse createEvent(BehaviorEventRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found."));

        User caregiver = null;
        if (request.getCaregiverId() != null) {
            caregiver = userRepository.findById(request.getCaregiverId())
                    .orElseThrow(() -> new RuntimeException("Caregiver not found."));
        }

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Appointment not found."));
        }

        ServiceDocumentation documentation = null;
        if (request.getServiceDocumentationId() != null) {
            documentation = serviceDocumentationRepository.findById(request.getServiceDocumentationId())
                    .orElseThrow(() -> new RuntimeException("Service documentation not found."));
        }

        BehaviorEvent event = BehaviorEvent.builder()
                .client(client)
                .caregiver(caregiver)
                .appointment(appointment)
                .serviceDocumentation(documentation)
                .behaviorType(request.getBehaviorType())
                .trigger(request.getTrigger())
                .severity(request.getSeverity())
                .durationMinutes(request.getDurationMinutes())
                .interventionUsed(request.getInterventionUsed())
                .outcome(request.getOutcome())
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(behaviorEventRepository.save(event));
    }

    public List<BehaviorEventResponse> getByClient(Long clientId) {
        return behaviorEventRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BehaviorEventResponse> getByServiceDocumentation(Long documentationId) {
        return behaviorEventRepository.findByServiceDocumentationIdOrderByCreatedAtDesc(documentationId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private BehaviorEventResponse mapToResponse(BehaviorEvent event) {
        return BehaviorEventResponse.builder()
                .id(event.getId())
                .clientId(event.getClient().getId())
                .clientName(event.getClient().getFullName())
                .caregiverId(event.getCaregiver() != null ? event.getCaregiver().getId() : null)
                .caregiverName(event.getCaregiver() != null ? event.getCaregiver().getFullName() : null)
                .appointmentId(event.getAppointment() != null ? event.getAppointment().getId() : null)
                .serviceDocumentationId(
                        event.getServiceDocumentation() != null
                                ? event.getServiceDocumentation().getId()
                                : null
                )
                .behaviorType(event.getBehaviorType())
                .trigger(event.getTrigger())
                .severity(event.getSeverity())
                .durationMinutes(event.getDurationMinutes())
                .interventionUsed(event.getInterventionUsed())
                .outcome(event.getOutcome())
                .notes(event.getNotes())
                .createdAt(event.getCreatedAt())
                .build();
    }
    public List<BehaviorOptionResponse> getBehaviorTypes() {
        return Arrays.asList(
                option("VERBAL_OUTBURST", "Verbal Outburst"),
                option("AGGRESSION", "Aggression"),
                option("SELF_INJURY", "Self Injury"),
                option("ELOPEMENT", "Elopement"),
                option("REFUSAL", "Refusal"),
                option("PROPERTY_DESTRUCTION", "Property Destruction")
        );
    }

    public List<BehaviorOptionResponse> getTriggers() {
        return Arrays.asList(
                option("UNKNOWN", "Unknown"),
                option("DEMAND_PLACED", "Demand Placed"),
                option("TRANSITION", "Transition"),
                option("DENIED_ACCESS", "Denied Access"),
                option("LOUD_NOISE", "Loud Noise")
        );
    }

    public List<BehaviorOptionResponse> getSeverities() {
        return Arrays.asList(
                option("LOW", "Low"),
                option("MEDIUM", "Medium"),
                option("HIGH", "High"),
                option("CRITICAL", "Critical")
        );
    }

    public List<BehaviorOptionResponse> getOutcomes() {
        return Arrays.asList(
                option("RESOLVED", "Resolved"),
                option("ESCALATED", "Escalated"),
                option("SUPERVISOR_NOTIFIED", "Supervisor Notified"),
                option("INCIDENT_REPORT_REQUIRED", "Incident Report Required")
        );
    }

    private BehaviorOptionResponse option(String value, String label) {
        return BehaviorOptionResponse.builder()
                .value(value)
                .label(label)
                .build();
    }
}