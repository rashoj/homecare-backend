package com.homecare.service;

import com.homecare.dto.AdminDashboardResponse;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DocumentRepository documentRepository;
    private final MedicationRepository medicationRepository;
    private final VisitNoteRepository visitNoteRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final IncidentRepository incidentRepository;
    private final ServiceDocumentationRepository serviceDocumentationRepository;
    private final MedicationLogRepository medicationLogRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            ClientRepository clientRepository,
            AppointmentRepository appointmentRepository,
            DocumentRepository documentRepository,
            MedicationRepository medicationRepository,
            VisitNoteRepository visitNoteRepository,
            ClockRecordRepository clockRecordRepository,
            IncidentRepository incidentRepository,
            ServiceDocumentationRepository serviceDocumentationRepository,
            MedicationLogRepository medicationLogRepository
    ) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.appointmentRepository = appointmentRepository;
        this.documentRepository = documentRepository;
        this.medicationRepository = medicationRepository;
        this.visitNoteRepository = visitNoteRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.incidentRepository = incidentRepository;
        this.serviceDocumentationRepository = serviceDocumentationRepository;
        this.medicationLogRepository = medicationLogRepository;
    }

    public AdminDashboardResponse getDashboard() {
        long totalMedicationLogs = medicationLogRepository.count();

        long administered =
                medicationLogRepository.countByStatus("GIVEN")
                        + medicationLogRepository.countByStatus("ADMINISTERED");

        double marComplianceRate = totalMedicationLogs == 0
                ? 100.0
                : Math.round((administered * 100.0 / totalMedicationLogs) * 100.0) / 100.0;

        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalCaregivers(userRepository.countByRoleName("CAREGIVER"))
                .totalClients(clientRepository.count())
                .totalAppointments(appointmentRepository.count())
                .completedAppointments(appointmentRepository.countByStatus("COMPLETED"))
                .pendingDocuments(documentRepository.countByApprovalStatus("PENDING"))
                .totalMedications(medicationRepository.count())
                .totalVisitNotes(visitNoteRepository.count())
                .totalClockRecords(clockRecordRepository.count())
                .missedAppointments(appointmentRepository.countByStatus("MISSED"))
                .openIncidents(incidentRepository.countByStatus("UNDER_REVIEW"))
                .pendingServiceDocumentation(serviceDocumentationRepository.countByStatus("SUBMITTED"))
                .marComplianceRate(marComplianceRate)
                .build();
    }
}