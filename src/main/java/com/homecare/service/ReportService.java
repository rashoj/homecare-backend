package com.homecare.service;

import com.homecare.dto.ReportSummaryResponse;
import com.homecare.entity.ClockRecord;
import com.homecare.entity.Role;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final DocumentRepository documentRepository;

    public ReportService(ClientRepository clientRepository,
                         UserRepository userRepository,
                         AppointmentRepository appointmentRepository,
                         ClockRecordRepository clockRecordRepository,
                         DocumentRepository documentRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.documentRepository = documentRepository;
    }

    public ReportSummaryResponse getSummary(Double clientRate, Double caregiverRate) {

        double totalHours = clockRecordRepository.findAll()
                .stream()
                .filter(record -> record.getTotalHours() != null)
                .mapToDouble(ClockRecord::getTotalHours)
                .sum();

        double estimatedRevenue = totalHours * clientRate;
        double estimatedPayroll = totalHours * caregiverRate;
        double estimatedGrossMargin = estimatedRevenue - estimatedPayroll;

        return ReportSummaryResponse.builder()
                .totalClients(clientRepository.count())
                .totalCaregivers(userRepository.countByRole(Role.CAREGIVER))
                .totalAppointments(appointmentRepository.count())
                .completedVisits(appointmentRepository.countByCompletedTrue())
                .totalHours(totalHours)
                .estimatedRevenue(estimatedRevenue)
                .estimatedPayroll(estimatedPayroll)
                .estimatedGrossMargin(estimatedGrossMargin)
                .pendingDocuments(documentRepository.countByApprovalStatus("PENDING"))
                .build();
    }
}