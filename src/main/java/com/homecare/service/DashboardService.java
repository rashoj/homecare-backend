package com.homecare.service;

import com.homecare.dto.*;
import com.homecare.entity.*;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DocumentRepository documentRepository;
    private final MedicationRepository medicationRepository;
    private final VisitNoteRepository visitNoteRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final IncidentRepository incidentRepository;
    private final ServiceDocumentationRepository serviceDocumentationRepository;
    private final EVVExceptionRepository evvExceptionRepository;
    private final AuditLogRepository auditLogRepository;

    public DashboardService(
            UserRepository userRepository,
            ClientRepository clientRepository,
            AppointmentRepository appointmentRepository,
            DocumentRepository documentRepository,
            MedicationRepository medicationRepository,
            VisitNoteRepository visitNoteRepository,
            ClockRecordRepository clockRecordRepository,
            MedicationLogRepository medicationLogRepository,
            IncidentRepository incidentRepository,
            ServiceDocumentationRepository serviceDocumentationRepository,
            EVVExceptionRepository evvExceptionRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.appointmentRepository = appointmentRepository;
        this.documentRepository = documentRepository;
        this.medicationRepository = medicationRepository;
        this.visitNoteRepository = visitNoteRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.medicationLogRepository = medicationLogRepository;
        this.incidentRepository = incidentRepository;
        this.serviceDocumentationRepository = serviceDocumentationRepository;
        this.evvExceptionRepository = evvExceptionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public AdminDashboardResponse getAdminDashboard() {
        long totalMARLogs = medicationLogRepository.count();

        long administered =
                medicationLogRepository.countByStatus("GIVEN")
                        + medicationLogRepository.countByStatus("ADMINISTERED");

        double marComplianceRate =
                totalMARLogs == 0 ? 0 : ((double) administered / totalMARLogs) * 100;

        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalCaregivers(userRepository.countByRole(Role.CAREGIVER))
                .totalClients(clientRepository.count())
                .totalAppointments(appointmentRepository.count())
                .completedAppointments(appointmentRepository.countByCompletedTrue())
                .pendingDocuments(documentRepository.countByApprovalStatus("PENDING"))
                .totalMedications(medicationRepository.count())
                .totalVisitNotes(visitNoteRepository.count())
                .totalClockRecords(clockRecordRepository.count())
                .missedAppointments(appointmentRepository.countByCompletedFalse())
                .openIncidents(incidentRepository.countByStatus("UNDER_REVIEW"))
                .pendingServiceDocumentation(serviceDocumentationRepository.countByStatus("SUBMITTED"))
                .marComplianceRate(Math.round(marComplianceRate * 100.0) / 100.0)
                .build();
    }

    public CaregiverDashboardResponse getCaregiverDashboard(Long caregiverId) {
        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver not found"));

        return CaregiverDashboardResponse.builder()
                .caregiverId(caregiver.getId())
                .caregiverName(caregiver.getFullName())
                .todayAppointments(appointmentRepository.countByCaregiverId(caregiverId))
                .completedAppointments(appointmentRepository.countByCaregiverIdAndCompletedTrue(caregiverId))
                .pendingAppointments(appointmentRepository.countByCaregiverIdAndCompletedFalse(caregiverId))
                .totalVisitNotes(visitNoteRepository.countByCaregiverId(caregiverId))
                .totalClockRecords(clockRecordRepository.countByAppointmentCaregiverId(caregiverId))
                .medicationLogs(medicationLogRepository.countByCaregiverId(caregiverId))
                .build();
    }

    public List<DashboardTrendResponse> getVisitTrends() {
        return last7Days().stream()
                .map(day -> {
                    long completed = appointmentRepository.findAll().stream()
                            .filter(a -> a.getStartTime() != null)
                            .filter(a -> a.getStartTime().toLocalDate().equals(day))
                            .filter(a -> Boolean.TRUE.equals(a.getCompleted()))
                            .count();

                    long missed = appointmentRepository.findAll().stream()
                            .filter(a -> a.getStartTime() != null)
                            .filter(a -> a.getStartTime().toLocalDate().equals(day))
                            .filter(a -> Boolean.FALSE.equals(a.getCompleted()))
                            .count();

                    return DashboardTrendResponse.builder()
                            .label(day.getDayOfWeek().toString().substring(0, 3))
                            .completed(completed)
                            .missed(missed)
                            .build();
                })
                .toList();
    }

    public List<DashboardTrendResponse> getMarTrends() {
        return last7Days().stream()
                .map(day -> {
                    List<MedicationLog> logsForDay = medicationLogRepository.findAll().stream()
                            .filter(log -> log.getScheduledAt() != null)
                            .filter(log -> log.getScheduledAt().toLocalDate().equals(day))
                            .toList();

                    long total = logsForDay.size();

                    long given = logsForDay.stream()
                            .filter(log ->
                                    "GIVEN".equalsIgnoreCase(log.getStatus()) ||
                                            "ADMINISTERED".equalsIgnoreCase(log.getStatus())
                            )
                            .count();

                    double rate = total == 0 ? 100.0 : (given * 100.0) / total;

                    return DashboardTrendResponse.builder()
                            .label(day.getDayOfWeek().toString().substring(0, 3))
                            .rate(Math.round(rate * 100.0) / 100.0)
                            .build();
                })
                .toList();
    }

    public List<DashboardBreakdownResponse> getIncidentSeverity() {
        return List.of(
                DashboardBreakdownResponse.builder()
                        .name("LOW")
                        .value(incidentRepository.findAll().stream()
                                .filter(i -> "LOW".equalsIgnoreCase(i.getSeverity()))
                                .count())
                        .build(),
                DashboardBreakdownResponse.builder()
                        .name("MEDIUM")
                        .value(incidentRepository.findAll().stream()
                                .filter(i -> "MEDIUM".equalsIgnoreCase(i.getSeverity()))
                                .count())
                        .build(),
                DashboardBreakdownResponse.builder()
                        .name("HIGH")
                        .value(incidentRepository.findAll().stream()
                                .filter(i -> "HIGH".equalsIgnoreCase(i.getSeverity()))
                                .count())
                        .build(),
                DashboardBreakdownResponse.builder()
                        .name("CRITICAL")
                        .value(incidentRepository.findAll().stream()
                                .filter(i -> "CRITICAL".equalsIgnoreCase(i.getSeverity()))
                                .count())
                        .build()
        );
    }

    public List<DashboardTrendResponse> getEVVTrends() {
        return last7Days().stream()
                .map(day -> {
                    long exceptions = evvExceptionRepository.findAll().stream()
                            .filter(e -> e.getCreatedAt() != null)
                            .filter(e -> e.getCreatedAt().toLocalDate().equals(day))
                            .count();

                    return DashboardTrendResponse.builder()
                            .label(day.getDayOfWeek().toString().substring(0, 3))
                            .exceptions(exceptions)
                            .build();
                })
                .toList();
    }

    public List<RecentActivityResponse> getRecentActivity() {
        return auditLogRepository.findAll().stream()
                .sorted(Comparator.comparing(AuditLog::getCreatedAt).reversed())
                .limit(8)
                .map(log -> RecentActivityResponse.builder()
                        .id(log.getId())
                        .action(log.getAction())
                        .actorName(log.getActorName())
                        .resourceType(log.getResourceType())
                        .resourceId(log.getResourceId())
                        .description(log.getDescription())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();
    }

    private List<LocalDate> last7Days() {
        LocalDate today = LocalDate.now();

        return List.of(
                today.minusDays(6),
                today.minusDays(5),
                today.minusDays(4),
                today.minusDays(3),
                today.minusDays(2),
                today.minusDays(1),
                today
        );
    }
}