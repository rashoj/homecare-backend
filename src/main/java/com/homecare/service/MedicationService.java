package com.homecare.service;

import com.homecare.dto.*;
import com.homecare.entity.Client;
import com.homecare.entity.MARSupervisorAction;
import com.homecare.entity.Medication;
import com.homecare.entity.MedicationLog;
import com.homecare.entity.User;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.MARSupervisorActionRepository;
import com.homecare.repository.MedicationLogRepository;
import com.homecare.repository.MedicationRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final MARSupervisorActionRepository marSupervisorActionRepository;
    private final AuditLogService auditLogService;

    public MedicationService(
            MedicationRepository medicationRepository,
            MedicationLogRepository medicationLogRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            MARSupervisorActionRepository marSupervisorActionRepository,
            AuditLogService auditLogService
    ) {
        this.medicationRepository = medicationRepository;
        this.medicationLogRepository = medicationLogRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.marSupervisorActionRepository = marSupervisorActionRepository;
        this.auditLogService = auditLogService;
    }

    public MedicationResponse createMedication(MedicationRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        User actor = getActor(request.getActorUserId());

        Medication medication = Medication.builder()
                .client(client)
                .medicationName(request.getMedicationName())
                .dosage(request.getDosage())
                .frequency(request.getFrequency())
                .scheduledTime(request.getScheduledTime())
                .instructions(request.getInstructions())
                .active(true)
                .build();

        Medication savedMedication = medicationRepository.save(medication);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                client.getId(),
                "CREATE_MEDICATION",
                "MEDICATION",
                savedMedication.getId(),
                "Medication created."
        );

        return mapMedicationToResponse(savedMedication);
    }

    public MedicationResponse updateMedication(Long id, MedicationRequest request) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        User actor = getActor(request.getActorUserId());

        medication.setMedicationName(request.getMedicationName());
        medication.setDosage(request.getDosage());
        medication.setFrequency(request.getFrequency());
        medication.setScheduledTime(request.getScheduledTime());
        medication.setInstructions(request.getInstructions());

        Medication savedMedication = medicationRepository.save(medication);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedMedication.getClient().getId(),
                "UPDATE_MEDICATION",
                "MEDICATION",
                savedMedication.getId(),
                "Medication updated."
        );

        return mapMedicationToResponse(savedMedication);
    }

    public void deleteMedication(Long id) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        medication.setActive(false);
        Medication savedMedication = medicationRepository.save(medication);

        auditLogService.logAction(
                null,
                "SYSTEM",
                "SYSTEM",
                savedMedication.getClient().getId(),
                "DEACTIVATE_MEDICATION",
                "MEDICATION",
                savedMedication.getId(),
                "Medication deactivated."
        );
    }

    public MedicationLogResponse logMedication(MedicationLogRequest request, String actorEmail) {
        Medication medication = medicationRepository.findById(request.getMedicationId())
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        User actor = userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));


        User caregiver = null;
        if (request.getCaregiverId() != null) {
            caregiver = userRepository.findById(request.getCaregiverId())
                    .orElseThrow(() -> new RuntimeException("Caregiver not found"));
        }

        LocalDateTime scheduledAt = null;
        if (request.getScheduledAt() != null && !request.getScheduledAt().isBlank()) {
            scheduledAt = LocalDateTime.parse(request.getScheduledAt());
        }

        if (scheduledAt != null &&
                medicationLogRepository.existsByMedicationIdAndClientIdAndScheduledAt(
                        medication.getId(),
                        medication.getClient().getId(),
                        scheduledAt
                )) {
            throw new RuntimeException("MAR entry already exists for this scheduled medication pass.");
        }

        MedicationLog log = MedicationLog.builder()
                .medication(medication)
                .client(medication.getClient())
                .caregiver(caregiver)
                .organization(medication.getClient().getOrganization())
                .scheduledAt(scheduledAt)
                .status(request.getStatus())
                .notes(request.getNotes())
                .prn(Boolean.TRUE.equals(request.getPrn()))
                .prnReason(request.getPrnReason())
                .refusalReason(request.getRefusalReason())
                .missedReason(request.getMissedReason())
                .caregiverSignature(request.getCaregiverSignature())
                .build();

        MedicationLog savedLog = medicationLogRepository.save(log);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedLog.getClient().getId(),
                getMARAuditAction(savedLog),
                "MEDICATION_LOG",
                savedLog.getId(),
                "Medication log recorded with status " + savedLog.getStatus() + "."
        );

        return mapLogToResponse(savedLog);
    }

    public List<MedicationResponse> getAllMedications() {
        return medicationRepository.findByActiveTrue()
                .stream()
                .map(this::mapMedicationToResponse)
                .toList();
    }

    public List<MedicationResponse> getMedicationsByClient(Long clientId) {
        return medicationRepository.findByClientIdAndActiveTrue(clientId)
                .stream()
                .map(this::mapMedicationToResponse)
                .toList();
    }

    public List<MedicationLogResponse> getMedicationLogsByClient(Long clientId) {
        return medicationLogRepository.findByClientId(clientId)
                .stream()
                .map(this::mapLogToResponse)
                .toList();
    }

    public List<MedicationLogResponse> getMedicationLogsByMedication(Long medicationId) {
        return medicationLogRepository.findByMedicationId(medicationId)
                .stream()
                .map(this::mapLogToResponse)
                .toList();
    }

    public List<MARDueMedicationResponse> getDueMedicationsByClient(Long clientId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();

        List<Medication> medications =
                medicationRepository.findByClientIdAndActiveTrue(clientId);

        return medications.stream()
                .map(medication -> {
                    LocalDateTime scheduledAt = startOfDay.plus(
                            java.time.Duration.between(
                                    java.time.LocalTime.MIDNIGHT,
                                    medication.getScheduledTime()
                            )
                    );

                    boolean alreadyLogged =
                            medicationLogRepository.existsByMedicationIdAndClientIdAndScheduledAt(
                                    medication.getId(),
                                    medication.getClient().getId(),
                                    scheduledAt
                            );

                    return MARDueMedicationResponse.builder()
                            .medicationId(medication.getId())
                            .clientId(medication.getClient().getId())
                            .clientName(medication.getClient().getFullName())
                            .medicationName(medication.getMedicationName())
                            .dosage(medication.getDosage())
                            .frequency(medication.getFrequency())
                            .scheduledTime(medication.getScheduledTime())
                            .scheduledAt(scheduledAt)
                            .instructions(medication.getInstructions())
                            .alreadyLogged(alreadyLogged)
                            .build();
                })
                .toList();
    }

    public List<MARAlertResponse> getMARAlerts() {
        List<MARAlertResponse> alerts = new ArrayList<>();

        List<MedicationLog> flaggedLogs = medicationLogRepository.findAll()
                .stream()
                .filter(log ->
                        "MISSED".equalsIgnoreCase(log.getStatus()) ||
                                "REFUSED".equalsIgnoreCase(log.getStatus()) ||
                                "HELD".equalsIgnoreCase(log.getStatus()) ||
                                "PRN_GIVEN".equalsIgnoreCase(log.getStatus())
                )
                .toList();

        for (MedicationLog log : flaggedLogs) {
            alerts.add(mapLogToAlert(log, log.getStatus()));
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<Medication> activeMedications = medicationRepository.findByActiveTrue();

        for (Medication medication : activeMedications) {
            if (medication.getScheduledTime() == null) {
                continue;
            }

            LocalDateTime scheduledAt = LocalDateTime.of(today, medication.getScheduledTime());

            boolean alreadyLogged =
                    medicationLogRepository.existsByMedicationIdAndClientIdAndScheduledAt(
                            medication.getId(),
                            medication.getClient().getId(),
                            scheduledAt
                    );

            if (!alreadyLogged && scheduledAt.isBefore(now)) {
                alerts.add(
                        MARAlertResponse.builder()
                                .alertType("OVERDUE")
                                .medicationId(medication.getId())
                                .medicationName(medication.getMedicationName())
                                .clientId(medication.getClient().getId())
                                .clientName(medication.getClient().getFullName())
                                .scheduledAt(scheduledAt)
                                .status("OVERDUE")
                                .reason("Medication due time passed and no MAR entry was logged.")
                                .notes(medication.getInstructions())
                                .build()
                );
            }
        }

        return alerts;
    }

    public List<MedicationLogResponse> getMARReviewLogs() {
        return medicationLogRepository.findAll()
                .stream()
                .map(this::mapLogToResponse)
                .toList();
    }

    public MARComplianceSummaryResponse getMARComplianceSummary() {
        long total = medicationLogRepository.count();

        long administered =
                medicationLogRepository.countByStatus("GIVEN")
                        + medicationLogRepository.countByStatus("ADMINISTERED");

        long missed = medicationLogRepository.countByStatus("MISSED");
        long refused = medicationLogRepository.countByStatus("REFUSED");
        long held = medicationLogRepository.countByStatus("HELD");
        long prnGiven = medicationLogRepository.countByStatus("PRN_GIVEN");

        long overdue = getMARAlerts()
                .stream()
                .filter(alert -> "OVERDUE".equals(alert.getAlertType()))
                .count();

        long expected = total + overdue;

        double complianceRate = expected == 0
                ? 100.0
                : (administered * 100.0) / expected;

        return MARComplianceSummaryResponse.builder()
                .totalMARLogs(total)
                .administeredCount(administered)
                .missedCount(missed)
                .refusedCount(refused)
                .heldCount(held)
                .prnGivenCount(prnGiven)
                .overdueCount(overdue)
                .marComplianceRate(Math.round(complianceRate * 100.0) / 100.0)
                .build();
    }

    public MARSupervisorActionResponse createMARSupervisorAction(
            MARSupervisorActionRequest request
    ) {
        MedicationLog medicationLog = medicationLogRepository
                .findById(request.getMedicationLogId())
                .orElseThrow(() -> new RuntimeException("Medication log not found"));

        User actor = getActor(request.getActorUserId());

        User supervisor = null;
        if (request.getSupervisorId() != null) {
            supervisor = userRepository.findById(request.getSupervisorId())
                    .orElseThrow(() -> new RuntimeException("Supervisor not found"));
        }

        MARSupervisorAction action = MARSupervisorAction.builder()
                .medicationLog(medicationLog)
                .supervisor(supervisor)
                .actionStatus(request.getActionStatus())
                .supervisorNotes(request.getSupervisorNotes())
                .build();

        MARSupervisorAction savedAction = marSupervisorActionRepository.save(action);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                medicationLog.getClient().getId(),
                "CREATE_MAR_SUPERVISOR_ACTION",
                "MAR_SUPERVISOR_ACTION",
                savedAction.getId(),
                "MAR supervisor action created with status " + savedAction.getActionStatus() + "."
        );

        return mapMARSupervisorActionToResponse(savedAction);
    }

    public List<MARSupervisorActionResponse> getMARSupervisorActionsByLog(
            Long medicationLogId
    ) {
        return marSupervisorActionRepository
                .findByMedicationLogIdOrderByCreatedAtDesc(medicationLogId)
                .stream()
                .map(this::mapMARSupervisorActionToResponse)
                .toList();
    }

    private User getActor(Long actorUserId) {
        if (actorUserId == null) {
            throw new RuntimeException("Actor user is required for audit logging.");
        }

        return userRepository.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("Actor user not found."));
    }

    private String getMARAuditAction(MedicationLog log) {
        String status = log.getStatus() != null ? log.getStatus().toUpperCase() : "UNKNOWN";

        if (Boolean.TRUE.equals(log.getPrn()) || "PRN_GIVEN".equals(status)) {
            return "PRN_MEDICATION_GIVEN";
        }

        if ("GIVEN".equals(status) || "ADMINISTERED".equals(status)) {
            return "ADMINISTER_MEDICATION";
        }

        if ("REFUSED".equals(status)) {
            return "REFUSE_MEDICATION";
        }

        if ("MISSED".equals(status)) {
            return "MISSED_MEDICATION";
        }

        if ("HELD".equals(status)) {
            return "HELD_MEDICATION";
        }

        return "CREATE_MEDICATION_LOG";
    }

    private MedicationResponse mapMedicationToResponse(Medication medication) {
        return MedicationResponse.builder()
                .id(medication.getId())
                .clientId(medication.getClient().getId())
                .clientName(medication.getClient().getFullName())
                .medicationName(medication.getMedicationName())
                .dosage(medication.getDosage())
                .frequency(medication.getFrequency())
                .scheduledTime(medication.getScheduledTime())
                .instructions(medication.getInstructions())
                .active(medication.getActive())
                .build();
    }

    private MedicationLogResponse mapLogToResponse(MedicationLog log) {
        return MedicationLogResponse.builder()
                .id(log.getId())
                .medicationId(log.getMedication().getId())
                .medicationName(log.getMedication().getMedicationName())
                .clientId(log.getClient().getId())
                .clientName(log.getClient().getFullName())
                .caregiverId(log.getCaregiver() != null ? log.getCaregiver().getId() : null)
                .caregiverName(log.getCaregiver() != null ? log.getCaregiver().getFullName() : null)
                .scheduledAt(log.getScheduledAt())
                .givenAt(log.getGivenAt())
                .status(log.getStatus())
                .notes(log.getNotes())
                .prn(log.getPrn())
                .prnReason(log.getPrnReason())
                .refusalReason(log.getRefusalReason())
                .missedReason(log.getMissedReason())
                .caregiverSignature(log.getCaregiverSignature())
                .build();
    }

    private MARAlertResponse mapLogToAlert(MedicationLog log, String alertType) {
        String reason =
                log.getMissedReason() != null ? log.getMissedReason()
                        : log.getRefusalReason() != null ? log.getRefusalReason()
                          : log.getPrnReason() != null ? log.getPrnReason()
                            : log.getNotes();

        return MARAlertResponse.builder()
                .alertType(alertType)
                .medicationId(log.getMedication().getId())
                .medicationName(log.getMedication().getMedicationName())
                .clientId(log.getClient().getId())
                .clientName(log.getClient().getFullName())
                .caregiverId(log.getCaregiver() != null ? log.getCaregiver().getId() : null)
                .caregiverName(log.getCaregiver() != null ? log.getCaregiver().getFullName() : null)
                .scheduledAt(log.getScheduledAt())
                .givenAt(log.getGivenAt())
                .status(log.getStatus())
                .reason(reason)
                .notes(log.getNotes())
                .build();
    }

    private MARSupervisorActionResponse mapMARSupervisorActionToResponse(
            MARSupervisorAction action
    ) {
        return MARSupervisorActionResponse.builder()
                .id(action.getId())
                .medicationLogId(action.getMedicationLog().getId())
                .supervisorId(action.getSupervisor() != null ? action.getSupervisor().getId() : null)
                .supervisorName(action.getSupervisor() != null ? action.getSupervisor().getFullName() : null)
                .actionStatus(action.getActionStatus())
                .supervisorNotes(action.getSupervisorNotes())
                .createdAt(action.getCreatedAt())
                .build();
    }
}