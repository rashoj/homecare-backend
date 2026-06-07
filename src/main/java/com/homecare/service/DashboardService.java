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

    public AdminDashboardResponse getAdminDashboard(String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);
        Long organizationId = organization.getId();

        List<User> organizationUsers =
                userRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId);

        long totalAgencyUsers = organizationUsers.size();

        long totalCaregivers = organizationUsers.stream()
                .filter(user -> user.getRole() == Role.CAREGIVER)
                .count();

        long totalClients =
                clientRepository.findByOrganizationIdAndActiveTrue(organizationId).size();

        return AdminDashboardResponse.builder()
                .totalUsers(totalAgencyUsers)
                .totalCaregivers(totalCaregivers)
                .totalClients(totalClients)
                .totalAppointments(appointmentRepository.countByOrganizationId(organizationId))
                .completedAppointments(appointmentRepository.countByOrganizationIdAndCompletedTrue(organizationId))
                .missedAppointments(appointmentRepository.countByOrganizationIdAndCompletedFalse(organizationId))
                .pendingDocuments(0L)
                .totalMedications(0L)
                .totalVisitNotes(0L)
                .totalClockRecords(0L)
                .openIncidents(
                        incidentRepository.countByOrganizationIdAndStatus(
                                organizationId,
                                "UNDER_REVIEW"
                        )
                )                .pendingServiceDocumentation(0L)
                .marComplianceRate(0.0)
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

    public List<DashboardTrendResponse> getVisitTrends(String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);
        Long organizationId = organization.getId();

        List<Appointment> organizationAppointments =
                appointmentRepository.findByOrganizationIdOrderByStartTimeDesc(organizationId);

        return last7Days().stream()
                .map(day -> {
                    long completed = organizationAppointments.stream()
                            .filter(a -> a.getStartTime() != null)
                            .filter(a -> a.getStartTime().toLocalDate().equals(day))
                            .filter(a -> Boolean.TRUE.equals(a.getCompleted()))
                            .count();

                    long missed = organizationAppointments.stream()
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

    public List<DashboardTrendResponse> getMarTrends(String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        Long organizationId = organization.getId();

        List<MedicationLog> organizationLogs =
                medicationLogRepository.findByOrganizationIdOrderByScheduledAtDesc(organizationId);

        return last7Days().stream()
                .map(day -> {
                    List<MedicationLog> logsForDay = organizationLogs.stream()
                            .filter(log -> log.getScheduledAt() != null)
                            .filter(log -> log.getScheduledAt().toLocalDate().equals(day))
                            .toList();

                    long total = logsForDay.size();

                    long given = logsForDay.stream()
                            .filter(log ->
                                    "GIVEN".equalsIgnoreCase(log.getStatus()) ||
                                            "ADMINISTERED".equalsIgnoreCase(log.getStatus()) ||
                                            "PRN_GIVEN".equalsIgnoreCase(log.getStatus())
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
    public List<DashboardBreakdownResponse> getIncidentSeverity(String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        Long organizationId = organization.getId();

        return List.of(
                DashboardBreakdownResponse.builder()
                        .name("LOW")
                        .value(incidentRepository.countByOrganizationIdAndSeverity(organizationId, "LOW"))
                        .build(),
                DashboardBreakdownResponse.builder()
                        .name("MEDIUM")
                        .value(incidentRepository.countByOrganizationIdAndSeverity(organizationId, "MEDIUM"))
                        .build(),
                DashboardBreakdownResponse.builder()
                        .name("HIGH")
                        .value(incidentRepository.countByOrganizationIdAndSeverity(organizationId, "HIGH"))
                        .build(),
                DashboardBreakdownResponse.builder()
                        .name("CRITICAL")
                        .value(incidentRepository.countByOrganizationIdAndSeverity(organizationId, "CRITICAL"))
                        .build()
        );
    }
    public List<DashboardTrendResponse> getEVVTrends(String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        Long organizationId = organization.getId();

        List<EVVException> organizationExceptions =
                evvExceptionRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId);

        return last7Days().stream()
                .map(day -> {
                    long exceptions = organizationExceptions.stream()
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
    public List<RecentActivityResponse> getRecentActivity(String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);
        Long organizationId = organization.getId();

        List<Long> organizationUserIds =
                userRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId)
                        .stream()
                        .map(User::getId)
                        .toList();

        if (organizationUserIds.isEmpty()) {
            return List.of();
        }

        return auditLogRepository.findByActorUserIdInOrderByCreatedAtDesc(organizationUserIds)
                .stream()
                .filter(log -> !List.of(
                        "ORGANIZATION_CREATED",
                        "AGENCY_ADMIN_CREATED",
                        "DEMO_REQUEST_CREATED",
                        "DEMO_REQUEST_STATUS_UPDATED",
                        "CONTACT_REQUEST_CREATED",
                        "CONTACT_REQUEST_STATUS_UPDATED"
                ).contains(log.getAction()))
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

    private User getActor(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Organization requireOrganization(User user) {
        if (user.getOrganization() == null ||
                user.getOrganization().getId() == null) {

            throw new RuntimeException(
                    "User is not assigned to an organization. userId="
                            + user.getId()
                            + ", email="
                            + user.getEmail()
                            + ", role="
                            + user.getRole()
            );
        }

        return user.getOrganization();
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