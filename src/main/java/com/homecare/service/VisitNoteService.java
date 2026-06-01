package com.homecare.service;

import com.homecare.dto.VisitNoteRequest;
import com.homecare.dto.VisitNoteResponse;
import com.homecare.entity.Appointment;
import com.homecare.entity.User;
import com.homecare.entity.VisitNote;
import com.homecare.repository.AppointmentRepository;
import com.homecare.repository.UserRepository;
import com.homecare.repository.VisitNoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitNoteService {

    private final VisitNoteRepository visitNoteRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AiSummaryService aiSummaryService;
    private final AuditLogService auditLogService;

    public VisitNoteService(
            VisitNoteRepository visitNoteRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            AiSummaryService aiSummaryService,
            AuditLogService auditLogService
    ) {
        this.visitNoteRepository = visitNoteRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.aiSummaryService = aiSummaryService;
        this.auditLogService = auditLogService;
    }

    public VisitNoteResponse createVisitNote(VisitNoteRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        User actor = userRepository.findById(request.getActorUserId())
                .orElseThrow(() -> new RuntimeException("Actor user not found."));

        if (visitNoteRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new RuntimeException("Visit note already exists for this appointment");
        }

        VisitNote visitNote = VisitNote.builder()
                .appointment(appointment)
                .client(appointment.getClient())
                .caregiver(appointment.getCaregiver())
                .generalNotes(request.getGeneralNotes())
                .meals(request.getMeals())
                .medicationNotes(request.getMedicationNotes())
                .mobilityNotes(request.getMobilityNotes())
                .moodNotes(request.getMoodNotes())
                .hygieneCare(request.getHygieneCare())
                .safetyConcerns(request.getSafetyConcerns())
                .familyUpdate(request.getFamilyUpdate())
                .incidentReported(request.getIncidentReported())
                .incidentDetails(request.getIncidentDetails())
                .build();

        visitNote.setAiSummary(
                aiSummaryService.generateVisitSummary(buildCombinedNote(request))
        );

        VisitNote savedVisitNote = visitNoteRepository.save(visitNote);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                appointment.getClient().getId(),
                "CREATE_VISIT_NOTE",
                "VISIT_NOTE",
                savedVisitNote.getId(),
                "Visit note created."
        );

        return mapToResponse(savedVisitNote);
    }

    public VisitNoteResponse updateVisitNote(Long id, VisitNoteRequest request) {
        VisitNote visitNote = visitNoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit note not found"));

        User actor = userRepository.findById(request.getActorUserId())
                .orElseThrow(() -> new RuntimeException("Actor user not found."));

        visitNote.setGeneralNotes(request.getGeneralNotes());
        visitNote.setMeals(request.getMeals());
        visitNote.setMedicationNotes(request.getMedicationNotes());
        visitNote.setMobilityNotes(request.getMobilityNotes());
        visitNote.setMoodNotes(request.getMoodNotes());
        visitNote.setHygieneCare(request.getHygieneCare());
        visitNote.setSafetyConcerns(request.getSafetyConcerns());
        visitNote.setFamilyUpdate(request.getFamilyUpdate());
        visitNote.setIncidentReported(request.getIncidentReported());
        visitNote.setIncidentDetails(request.getIncidentDetails());

        visitNote.setAiSummary(
                aiSummaryService.generateVisitSummary(buildCombinedNote(request))
        );

        VisitNote savedVisitNote = visitNoteRepository.save(visitNote);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedVisitNote.getClient().getId(),
                "UPDATE_VISIT_NOTE",
                "VISIT_NOTE",
                savedVisitNote.getId(),
                "Visit note updated."
        );

        return mapToResponse(savedVisitNote);
    }

    public VisitNoteResponse regenerateAiSummary(Long id, VisitNoteRequest request) {
        VisitNote visitNote = visitNoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit note not found"));

        User actor = userRepository.findById(request.getActorUserId())
                .orElseThrow(() -> new RuntimeException("Actor user not found."));

        String aiSummary = aiSummaryService.generateVisitSummary(buildCombinedNote(visitNote));

        visitNote.setAiSummary(aiSummary);

        VisitNote savedVisitNote = visitNoteRepository.save(visitNote);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedVisitNote.getClient().getId(),
                "REGENERATE_VISIT_NOTE_AI_SUMMARY",
                "VISIT_NOTE",
                savedVisitNote.getId(),
                "Visit note AI summary regenerated."
        );

        return mapToResponse(savedVisitNote);
    }

    public List<VisitNoteResponse> getAllVisitNotes() {
        return visitNoteRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public VisitNoteResponse getVisitNoteById(Long id) {
        VisitNote visitNote = visitNoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit note not found"));

        auditLogService.logAction(
                visitNote.getCaregiver().getId(),
                visitNote.getCaregiver().getFullName(),
                visitNote.getCaregiver().getRole().name(),
                visitNote.getClient().getId(),
                "VIEW_VISIT_NOTE",
                "VISIT_NOTE",
                visitNote.getId(),
                "Visit note viewed."
        );

        return mapToResponse(visitNote);
    }

    public VisitNoteResponse getVisitNoteByAppointment(Long appointmentId) {
        VisitNote visitNote = visitNoteRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Visit note not found"));

        return mapToResponse(visitNote);
    }

    public List<VisitNoteResponse> getVisitNotesByClient(Long clientId) {
        return visitNoteRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<VisitNoteResponse> getVisitNotesByCaregiver(Long caregiverId) {
        return visitNoteRepository.findByCaregiverId(caregiverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private String buildCombinedNote(VisitNoteRequest request) {
        return """
                General Notes: %s
                Meals: %s
                Medication Notes: %s
                Mobility Notes: %s
                Mood Notes: %s
                Hygiene Care: %s
                Safety Concerns: %s
                Family Update: %s
                Incident Reported: %s
                Incident Details: %s
                """.formatted(
                request.getGeneralNotes(),
                request.getMeals(),
                request.getMedicationNotes(),
                request.getMobilityNotes(),
                request.getMoodNotes(),
                request.getHygieneCare(),
                request.getSafetyConcerns(),
                request.getFamilyUpdate(),
                request.getIncidentReported(),
                request.getIncidentDetails()
        );
    }

    private String buildCombinedNote(VisitNote visitNote) {
        return """
                General Notes: %s
                Meals: %s
                Medication Notes: %s
                Mobility Notes: %s
                Mood Notes: %s
                Hygiene Care: %s
                Safety Concerns: %s
                Family Update: %s
                Incident Reported: %s
                Incident Details: %s
                """.formatted(
                visitNote.getGeneralNotes(),
                visitNote.getMeals(),
                visitNote.getMedicationNotes(),
                visitNote.getMobilityNotes(),
                visitNote.getMoodNotes(),
                visitNote.getHygieneCare(),
                visitNote.getSafetyConcerns(),
                visitNote.getFamilyUpdate(),
                visitNote.getIncidentReported(),
                visitNote.getIncidentDetails()
        );
    }

    private VisitNoteResponse mapToResponse(VisitNote visitNote) {
        return VisitNoteResponse.builder()
                .id(visitNote.getId())
                .appointmentId(visitNote.getAppointment().getId())
                .clientId(visitNote.getClient().getId())
                .clientName(visitNote.getClient().getFullName())
                .caregiverId(visitNote.getCaregiver().getId())
                .caregiverName(visitNote.getCaregiver().getFullName())
                .generalNotes(visitNote.getGeneralNotes())
                .meals(visitNote.getMeals())
                .medicationNotes(visitNote.getMedicationNotes())
                .mobilityNotes(visitNote.getMobilityNotes())
                .moodNotes(visitNote.getMoodNotes())
                .hygieneCare(visitNote.getHygieneCare())
                .safetyConcerns(visitNote.getSafetyConcerns())
                .familyUpdate(visitNote.getFamilyUpdate())
                .aiSummary(visitNote.getAiSummary())
                .incidentReported(visitNote.getIncidentReported())
                .incidentDetails(visitNote.getIncidentDetails())
                .createdAt(visitNote.getCreatedAt())
                .updatedAt(visitNote.getUpdatedAt())
                .build();
    }
}