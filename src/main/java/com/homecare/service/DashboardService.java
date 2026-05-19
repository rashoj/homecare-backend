package com.homecare.service;

import com.homecare.dto.AdminDashboardResponse;
import com.homecare.dto.CaregiverDashboardResponse;
import com.homecare.entity.Role;
import com.homecare.entity.User;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

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

    public DashboardService(UserRepository userRepository,
                            ClientRepository clientRepository,
                            AppointmentRepository appointmentRepository,
                            DocumentRepository documentRepository,
                            MedicationRepository medicationRepository,
                            VisitNoteRepository visitNoteRepository,
                            ClockRecordRepository clockRecordRepository,
                            MedicationLogRepository medicationLogRepository) {

        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.appointmentRepository = appointmentRepository;
        this.documentRepository = documentRepository;
        this.medicationRepository = medicationRepository;
        this.visitNoteRepository = visitNoteRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.medicationLogRepository = medicationLogRepository;
    }

    public AdminDashboardResponse getAdminDashboard() {

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
                .build();
    }

    public CaregiverDashboardResponse getCaregiverDashboard(Long caregiverId) {

        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver not found"));

        return CaregiverDashboardResponse.builder()
                .caregiverId(caregiver.getId())
                .caregiverName(caregiver.getFullName())
                .todayAppointments(
                        appointmentRepository.countByCaregiverId(caregiverId)
                )
                .completedAppointments(
                        appointmentRepository.countByCaregiverIdAndCompletedTrue(caregiverId)
                )
                .pendingAppointments(
                        appointmentRepository.countByCaregiverIdAndCompletedFalse(caregiverId)
                )
                .totalVisitNotes(
                        visitNoteRepository.countByCaregiverId(caregiverId)
                )
                .totalClockRecords(
                        clockRecordRepository.countByAppointmentCaregiverId(caregiverId)
                )
                .medicationLogs(
                        medicationLogRepository.countByCaregiverId(caregiverId)
                )
                .build();
    }
}